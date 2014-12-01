package controllers;

import models.User;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.login.*;
import views.html.latch.*;

import models.*;

/**
 * Created by Enri on 1/12/14.
 */
public class LatchController extends Controller {

    /**
     * Defines a form wrapping the User class.
     */
    final static Form<PairingKey> pairingKeyForm = form(PairingKey.class);

    /**
     * Display a blank form.
     */
    public static Result blank() {

        return ok(pair.render(pairingKeyForm));

    }

    /**
     * Handle the pairing.
     */
    public static Result pair() {
        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        // Check if pairing key is valid
        if(!filledForm.field("key").valueOr("").isEmpty()) {
            filledForm.reject("key", "You need to specify a pairing key");
        }

        if(filledForm.hasErrors()) {
            return badRequest(pair.render(filledForm));
        } else {
            User created = new User("borrar","borrar@email.com","password",new User.Profile("nopais", null, 1));
            return ok(unpair.render(created));
        }
    }

    /**
     * Handle the form submission.
     */
    public static Result unpair() {
        /*if(filledForm.hasErrors()) {
            return badRequest(pair.render(filledForm));
        } else {*/
            Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();
            return ok(pair.render(filledForm));
        //}
    }

}