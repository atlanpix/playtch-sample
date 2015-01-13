package controllers;

import actions.LatchPair;
import actions.LatchUnpair;
import com.elevenpaths.latch.Latch;
import com.elevenpaths.latch.LatchResponse;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.datasource.UserDataSource;
import models.entities.PairingKey;
import models.entities.User;
import pairingkey.ObtainPairingKey;
import play.Logger;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import com.google.gson.JsonObject;

import utils.factories.LatchIdFactory;
import utils.factories.LatchIdFromSessionFactory;
import utils.factories.PairingKeyFactory;
import views.html.login.*;
import views.html.latch.*;

/**
 * Created by Enri on 1/12/14.
 */
public class PairController extends Controller {

    /**
     * Defines a form wrapping the User class.
     */
    final static Form<PairingKey> pairingKeyForm = form(PairingKey.class);
    final static Form<User> loginForm = form(User.class);

    /**
     * Display a blank form.
     */
    public static Result blank() {
        Logger.debug(pairingKeyForm.bindFromRequest().toString());
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));
        if (user != null){
            Logger.debug("LATCH ACCOUNT ID: " + user.latchAccountId);
            if (!(user.latchAccountId.equals("null") || user.latchAccountId.equals(""))){
                return ok(views.html.latch.unpair.render());
            } else {
                return ok(views.html.latch.pair.render(pairingKeyForm));
            }
        }
        Form<User> filledForm = loginForm.bindFromRequest();
        return forbidden(views.html.login.login.render(loginForm));
    }

    /**
     * Handle the pair action.
     */
    @LatchPair(pairingKey = PairingKeyFactory.class)
    public static Result pair() {
        String accountId = (String) Http.Context.current().args.get("status");

        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        // Check if pairing key is valid
        if(filledForm.field("key").valueOr("").isEmpty()) {
            filledForm.reject("key", "You need to specify a pairing key");
        }

        if(filledForm.hasErrors()) {
            return badRequest(views.html.latch.pair.render(filledForm));
        } else {
            if (accountId != null) {
                UserDataSource userDataSource = new UserDataSource();
                User user = userDataSource.getUser(session("username"));
                userDataSource.updateLatchAccountId(user.username, accountId);
                Logger.debug("Pair success!");
                return ok(views.html.latch.unpair.render());
            } else {
                Logger.debug("<Error> Pair fail");
                return badRequest(views.html.latch.pair.render(filledForm));
            }
        }
    }

    /**
     * Handle the unpair action.
     */
    @LatchUnpair(latchId = LatchIdFromSessionFactory.class)
    public static Result unpair() {
        String status = (String) Http.Context.current().args.get("status");
        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));

        if (user != null) {
            if (status != null) {
                userDataSource.updateLatchAccountId(user.username,"");
                return badRequest(views.html.latch.pair.render(filledForm));
            } else {
                Logger.debug("<Error> Unpair fail");
                return badRequest(views.html.latch.unpair.render());
            }
        }
        return badRequest(views.html.latch.pair.render(filledForm));
    }

}