package utils.factories;

import latchid.ObtainLatchId;
import models.datasource.UserDataSource;
import models.entities.User;
import play.mvc.Http;

/**
 * Created by Enri on 13/1/15.
 */
public class LatchIdFromSessionFactory implements ObtainLatchId {

    @Override
    public String getLatchId(Http.Context context) {
        String userId = context.session().get("username");
        UserDataSource userDataSource = new UserDataSource();
        User user = userDataSource.getUser(userId);

        if (!(user.latchAccountId.equals("null") || user.latchAccountId.equals(""))){
            return user.latchAccountId;
        }
        return null;
    }

}
