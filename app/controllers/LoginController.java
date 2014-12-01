package controllers;

import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.login.*;
import views.html.latch.*;

import models.*;

public class LoginController extends Controller {
    
    /**
     * Defines a form wrapping the User class.
     */ 
    final static Form<User> loginForm = form(User.class, User.All.class);
    final static Form<PairingKey> pairingKeyForm = form(PairingKey.class);
  
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
        if(!filledForm.field("password").valueOr("").isEmpty()) {
            if(!filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value())) {
                filledForm.reject("repeatPassword", "Password don't match");
            }
        }
        
        // Check if the username is valid
        if(!filledForm.hasErrors()) {
            if(filledForm.get().username.equals("admin") || filledForm.get().username.equals("guest")) {
                filledForm.reject("username", "This username is already taken");
            }
        }
        
        if(filledForm.hasErrors()) {
            return badRequest(login.render(filledForm));
        } else {
            /*if(user.isPaired()){
                User created = filledForm.get();
            } else if (!user.isPaired()) {*/
                Form<PairingKey> filledFormKey = pairingKeyForm.bindFromRequest();
                return ok(pair.render(filledFormKey));
            //}
        }
    }
  
}