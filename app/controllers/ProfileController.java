package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.datasource.UserDataSource;
import models.entities.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import static play.data.Form.form;

import views.html.profile.*;
import views.html.index;

import java.util.Date;

/**
 * Created by Enri on 7/1/15.
 */
public class ProfileController extends Controller {

    /**
     * Defines a form wrapping the Profile class.
     */
    final static Form<User> profileForm = form(User.class);

    static Config config = ConfigFactory.load("application");

    /**
     * Display a blank form.
     */
    public static Result blank() {
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));
        if (user != null){
            return ok(form.render(profileForm.fill(user)));
        }
        // User doesn't exist, so logout and go to index
        Logger.error("User '"+session("username")+"' doesn't exist");
        /*session().clear();
        return ok(index.render());*/

        return ok(form.render(profileForm.fill(new User()))); // In case you want to show clear profile form page
    }

    public static Result edit() {
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));
        if (user != null){
            return ok(form.render(profileForm.fill(user)));
        }
        return ok(form.render(profileForm.fill(null)));
    }

    /**
     * Handle the form submission.
     */
    public static Result submit() {
        Form<User> filledForm = profileForm.bindFromRequest();

        if(filledForm.hasErrors()) {
            return badRequest(form.render(filledForm));
        } else {
            // Check latch
            boolean isLatchOn = true;
            //String status = LatchController.checkLatchStatus(config.getString("latch.appId"));
            String status = LatchController.checkLatchOperationStatus(config.getString("latch.editProfileOperationId"));

            Logger.debug("Status: " + status.toString());
            if(status.equals("off")){
                Logger.debug("<Error> [Checking status] Latch is OFF");
                isLatchOn = false;
            }
            Logger.debug("[Checking status] Latch is ON");

            if(isLatchOn){
                // Everything OK, we enter
                User created = filledForm.get();
                UserDataSource userDataSource = new UserDataSource();
                User newUser = userDataSource.getUser(created.username);
                if (created.username.equals(session("username")) || newUser == null){
                    userDataSource.updateUser(session("username"),created);
                    session("username",created.username);
                    return ok(summary.render(created));
                }
                filledForm.error("Username '"+created.username+"' is already taken");
                filledForm.reject("username", "Username '"+created.username+"' is already taken");
                return badRequest(form.render(filledForm));
            }
            // <Error> Tiene latch bloqueado
            Logger.debug("<Error> Latch is OFF");

            filledForm.error("-");
            filledForm.reject("username", "Error while editing your profile");
            return badRequest(form.render(filledForm));
        }
    }
}
