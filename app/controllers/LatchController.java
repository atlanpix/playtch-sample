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
import com.google.gson.JsonObject;

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
    final static Form<User> loginForm = form(User.class);

    static Config config = ConfigFactory.load("application");

    /**
     * Display a blank form.
     */
    public static Result blank() {
        Logger.debug(pairingKeyForm.bindFromRequest().toString());
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));
        if (user != null){
            if (!user.latchAccountId.equals("") || user.latchAccountId != null || !user.latchAccountId.isEmpty()){
                return ok(views.html.latch.unpair.render());
            } else {
                return ok(views.html.latch.pair.render(pairingKeyForm));
            }
        }
        Form<User> filledForm = loginForm.bindFromRequest();
        return forbidden(views.html.login.login.render(loginForm));
    }

    /**
     * Return a Latch object
     * */
    public static Latch getLatch(){
        return new Latch(config.getString("latch.appId"), config.getString("latch.secretKey"));
    }

    /**
     * Handle the pair action.
     */
    public static Result pair() {

        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        // Check if pairing key is valid
        if(filledForm.field("key").valueOr("").isEmpty()) {
            filledForm.reject("key", "You need to specify a pairing key");
        }

        if(filledForm.hasErrors()) {
            return badRequest(views.html.latch.pair.render(filledForm));
        } else {
            // REMEMBER: sudo keytool -import -noprompt -trustcacerts -alias CACertificate -file ca.pem -keystore "/Library/Java/JavaVirtualMachines/jdk1.7.0_67.jdk/Contents/Home/jre/lib/security/cacerts" -storepass changeit
            Latch latch = LatchController.getLatch();
            Logger.debug("Key: "+filledForm.get().key);
            LatchResponse response = latch.pair(filledForm.get().key);

            if (response != null && response.getError() == null) {

                String json = response.toJSON().toString();

                Logger.debug("JSON " + json);
                if (response.getData() != null) {
                    String accountId = response.getData().get("accountId").getAsString();
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
        return badRequest(views.html.latch.pair.render(filledForm));
    }

    /**
     * Handle the unpair action.
     */
    public static Result unpair() {
        Form<PairingKey> filledForm = pairingKeyForm.bindFromRequest();

        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));

        if (user != null) {
            LatchResponse response = LatchController.getLatch().unpair(user.latchAccountId);

            if (response != null && response.getError() == null) {
                String json = response.toJSON().toString();
                Logger.debug("JSON " + json);
                userDataSource.updateLatchAccountId(user.username,"");
                return badRequest(views.html.latch.pair.render(filledForm));
            } else {
                Logger.debug("<Error> Pair fail");
                return badRequest(views.html.latch.unpair.render());
            }
        }
        return badRequest(views.html.latch.pair.render(filledForm));
    }

}