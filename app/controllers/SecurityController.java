package controllers;

import play.mvc.Controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by Enri on 5/12/14.
 */
public class SecurityController extends Controller {
    private static SecureRandom random = new SecureRandom();

    public static String createSecretToken(){
        /*UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;*/

        return new BigInteger(130, random).toString(32);
    }
}
