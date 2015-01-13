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
     * Return a Latch object
     * */
    public static Latch getLatch(){
        return new Latch(config.getString("latch.appId"), config.getString("latch.secretKey"));
    }

    public static String checkLatchStatus(String appId){

        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));

        String accountId = user.latchAccountId;

        if (!(accountId.equals("null") || accountId.equals(""))){
            Latch latch = LatchController.getLatch();
            if (latch != null) {
                LatchResponse response = latch.status(accountId);

                // Para controlar una opración, habría que cambiar la llamada status a :
                // latch.operationStatus(accountID, "El id de la operation")
                if (response.getData() != null) {
                    String status = response.
                            getData().
                            get("operations").
                            getAsJsonObject().
                            get(appId).
                            getAsJsonObject().
                            get("status").getAsString();

                    // Para operaciones: String status = response.getData().get("operations").getAsJsonObject().get("El id de la operation").getAsJsonObject().get("status").getAsString();
                    if (!status.equals("") || status != null || !status.isEmpty()) {
                        Logger.debug("Status: " + status.toString());
                        return status;
                    }
                } else {
                    // We can decide to block user if Latch server is off: return "off";
                    // But we are going to be allowed
                }
            } else {
                // We can decide to block user if Latch server is off: return "off";
                // But we are going to be allowed
            }
        }
        return "on";
    }

    public static String checkLatchOperationStatus(String operationId){

        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(session("username"));

        String accountId = user.latchAccountId;

        if (!(accountId.equals("null") || accountId.equals(""))){
            Latch latch = LatchController.getLatch();
            if (latch != null) {
                LatchResponse response = latch.operationStatus(accountId, operationId);

                if (response.getData() != null) {
                    String status = response.
                            getData().
                            get("operations").
                            getAsJsonObject().
                            get(operationId).
                            getAsJsonObject().
                            get("status").getAsString();

                    if (!status.equals("") || status != null || !status.isEmpty()) {
                        Logger.debug("Status: " + status.toString());
                        return status;
                    }
                } else {
                    // We can decide to block user if Latch server is off: return "off";
                    // But we are going to be allowed
                }
            } else {
                // We can decide to block user if Latch server is off: return "off";
                // But we are going to be allowed
            }
        }
        return "on";
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
            } else {
                int errorCode = response.toJSON().get("error").getAsJsonObject().get("code").getAsInt();
                String errorMessage = response.toJSON().get("error").getAsJsonObject().get("message").getAsString();
                Logger.debug(errorMessage);
                filledForm.reject("pairingError", errorMessage);
                filledForm.reject("pairingError");
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

    /**
     * Handle OTP
     * */
    /*public static Result checkOTP{

        if(LatchController.checkIfAlmostAuthenticated())
        {


        } else {

            return
        }

    }*/
    /*
    public static function checkOtp(){
        self::checkIfAlmostAuthenticated();
        $storeOtp = DBHelper::getAndRemoveOtp($_SESSION["userId"]);
        if ($storeOtp == $_POST["otp"]){
            $_SESSION["loggedIn"] = true;
            unset($_SESSION["almostAuthenticated"]);
            header("Location: index.php?action=profile");
            die();
        } else {
            session_unset();
            setMsg("error", "Wrong second factor");
            header("Location index.php?action=login");
            die();
        }
    }

        if(!isset($_SESSION["almostAuthenticated"])){
            header("Location: index.php?action=logout");
        }
    */
    public static boolean checkIfAlmostAuthenticated(){
        if (!session("almostAuthenticated").isEmpty()
            || session("almostAuthenticated") != null
            || !session("almostAuthenticated").equals(""))
        {
            return false;
        }
        return true;
    }
}