package utils.factories;

import pairingkey.ObtainPairingKey;
import play.Logger;
import play.mvc.Http;

/**
 * Created by Enri on 13/1/15.
 */
public class PairingKeyFactory implements ObtainPairingKey {

    @Override
    public String getPairingKey(Http.Context context) {
        return context.request().body().asFormUrlEncoded().get("key")[0].toString();
    }

}
