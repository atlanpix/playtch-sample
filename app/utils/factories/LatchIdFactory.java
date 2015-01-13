package utils.factories;

import latchid.ObtainLatchId;
import models.datasource.UserDataSource;
import models.entities.User;
import play.mvc.Http;

/**
 * Created by Enri on 13/1/15.
 */
public class LatchIdFactory implements ObtainLatchId {

    @Override
    public String getLatchId(Http.Context context) {
        System.out.println("getLatchId:context");
        String userId = context.request().body().asFormUrlEncoded().get("username")[0].toString();
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(userId);

        if (!(user.latchAccountId.equals("null") || user.latchAccountId.equals(""))){
            System.out.println("getLatchId:user.latchAccountId - "+user.latchAccountId);
            return user.latchAccountId;
        }
        System.out.println("getLatchId:No tiene latchAccountId");
        return null;
    }

}
