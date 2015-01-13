package controllers;

import actions.LatchCheckOperationStatus;
import actions.LogMe;
import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.datasource.UserDataSource;
import models.entities.PairingKey;
import models.entities.User;
import play.Logger;
import play.data.validation.Validation;
import play.data.validation.ValidationError;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.*;
import views.html.login.*;
import views.html.latch.*;

import java.util.Date;

import controllers.MyLogger.MyLogger;
import play.mvc.*;
import actions.LogMe;
import models.datasource.UserDataSource;

public class LoginController extends Controller {
    
    /**
     * Defines a form wrapping the User class.
     */ 
    final static Form<User> loginForm = form(User.class, User.All.class);
    final static Form<PairingKey> pairingKeyForm = form(PairingKey.class);

    static Config config = ConfigFactory.load("application");
  
    /**
     * Display a blank form.
     */

    public static Result blank() {
        return ok(login.render(loginForm));
    }

    /**
     * Handle the form submission.
     */
    public static Result submit() {
        Form<User> filledForm = loginForm.bindFromRequest();
        
        // Check repeated password
        if(filledForm.field("password").valueOr("").isEmpty()) {
            filledForm.reject("password", "Enter a valid password");
        }
        
        // Check if the username is valid
        if(!filledForm.hasErrors()) {
            if(filledForm.get().username.equals("admin") || filledForm.get().username.equals("guest")) {
                filledForm.reject("username", "This username is already taken");
            }
        }
        
        if(filledForm.hasErrors()) {
            Logger.debug("Error with form");
            return badRequest(login.render(filledForm));
        } else {
            UserDataSource userDataSource = new UserDataSource();
            User user = userDataSource.getUser(filledForm.get().username);
            if (user != null){
                session("username", user.username);
                session("email", user.email);
                session("latchAccountId", user.latchAccountId);
                Logger.info("User no es null! Password introducido: "+ filledForm.get().password+ "Password del user: "+user.password );
                if (user.password.equals(filledForm.get().password)){
                    return login(user);
                }
            }
            Logger.debug("<Error> User can't login");
            session().clear();
            filledForm.reject("username","¡Error with login credentials!");
            return unauthorized(login.render(filledForm));
        }
    }

    @LatchCheckOperationStatus(value = "xjbieia3cvVVdv49MZar", latchId = UserDataSource.class)
    static Result login(User user){
        Logger.debug("Entra en login");
        // If user is authenticated
        // Check secretToken
        if (user.secretTokenExpiration == null || user.secretTokenExpiration.before(new Date())){
            Logger.debug("<Warning> Update secretToken because it was past");
            UserDataSource userDataSource = new UserDataSource();
            userDataSource.updateSecretToken(user.username);
        }
        return LatchController.blank();
    }

    /*public static Result login(User user){
        // If user is authenticated
        // Check latch status
        boolean isLatchOn = true;
        //String status = LatchController.checkLatchStatus(config.getString("latch.appId"));
        String status = LatchController.checkLatchOperationStatus(config.getString("latch.loginOperationId"));

        Logger.debug("Status: " + status.toString());
        if(status.equals("off")){
            Logger.debug("<Error> [Checking status] Latch is OFF");
            isLatchOn = false;
        }
        Logger.debug("[Checking status] Latch is ON");

        if(isLatchOn){
            // Everything OK, we enter
            Logger.debug("Latch is ON or is not paired");
            if (user.secretTokenExpiration == null || user.secretTokenExpiration.before(new Date())){
                Logger.debug("Update secretToken because it was past");
                UserDataSource userDataSource = new UserDataSource();
                userDataSource.updateSecretToken(user.username);
            }
            return LatchController.blank();
        }
        // <Error> Tiene latch bloqueado
        Logger.debug("<Error> Latch is OFF");
        session().clear();
        Form<User> filledForm = loginForm.bindFromRequest();
        filledForm.reject("username","¡Error with login credentials!");
        return unauthorized(login.render(filledForm));
    }*/

    /**
     * Handle logout
     * */
    public static Result logout(){
        session().clear();
        return ok(index.render());
    }
}