package controllers;

import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.datasource.UserDataSource;
import models.entities.PairingKey;
import models.entities.User;
import play.Logger;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.login.*;
import views.html.latch.*;

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
            Logger.debug("Error");
            return badRequest(login.render(filledForm));
        } else {
            Logger.debug("Va bien");
            UserDataSource userDataSource = new UserDataSource();
            User user = userDataSource.getUser(filledForm.get().username);
            if (user != null){
                Logger.info("User no es null! Password introducido: "+ filledForm.get().password+ "Password del user: "+user.password );
                if (user.password.equals(filledForm.get().password)){
                    Logger.debug("Password correcto! Password introducido: "+ filledForm.get().password+ "Password del user: "+user.password );

                    // Check latch
                    boolean isLatchOn = true;
                    String accountId = user.latchAccountId;
                    if (!accountId.equals("") || accountId != null || !accountId.isEmpty()){
                        String appId = config.getString("latch.appId");
                        String secretKey = config.getString("latch.secretKey");

                        Latch latch = new Latch(appId, secretKey);
                        LatchResponse response = latch.status(accountId);
                        // Para controlar una opración, habría que cambiar la llamada status a : latch.operationStatus(accountID, "El id de la operation")

                        String status = response.
                                getData().
                                get("operations").
                                getAsJsonObject().
                                get(appId).
                                getAsJsonObject().
                                get("status").getAsString();

                        // Para operaciones: String status = response.getData().get("operations").getAsJsonObject().get("El id de la operation").getAsJsonObject().get("status").getAsString();

                        Logger.debug("Status: " + status.toString());
                        if(status.equals("off")){
                            Logger.debug("<Error> [Checking status] Latch is OFF");
                            isLatchOn = false;
                        }
                        Logger.debug("[Checking status] Latch is ON");
                    }

                    if(isLatchOn){
                        // Everything OK, we enter
                        Logger.debug("Latch is ON or is not paired");
                        session("username", user.username);
                        session("email", user.email);
                        session("latchAccountId", user.latchAccountId);
                        return LatchController.blank();
                    }
                    // <Error> Tiene latch bloqueado
                    Logger.debug("<Error> Latch is OFF");
                }
            }
            Logger.debug("<Error> User can't login");
            return unauthorized(login.render(filledForm));
        }
    }
  
}