package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.entities.PairingKey;
import models.entities.User;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import views.html.login.*;
import views.html.latch.*;

/**
 * Created by Enri on 1/12/14.
 */
public class LatchController extends Controller {

    /**
     * Defines a form wrapping the User class.
     */
    final static Form<PairingKey> pairingKeyForm = form(PairingKey.class);

    static Config config = ConfigFactory.load("application");

    /**
     * Display a blank form.
     */
    public static Result blank() {

        return ok(views.html.latch.pair.render(pairingKeyForm));

    }

    /**
     * Handle the pairing.
     */
    public static Result pair() {

        String appId = config.getString("latch.appId");
        String secretKey = config.getString("latch.secretKey");

        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        // Check if pairing key is valid
        if(filledForm.field("key").valueOr("").isEmpty()) {
            filledForm.reject("key", "You need to specify a pairing key");
        }

        if(filledForm.hasErrors()) {
            return badRequest(pair.render(filledForm));
        } else {
            User created = new User("borrar","borrar@email.com","password");
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