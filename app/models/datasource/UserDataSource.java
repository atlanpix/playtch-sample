package models.datasource;

import com.mongodb.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import models.entities.*;
import play.Logger;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Nerea on 15/10/2014.
 */
public class UserDataSource {

    public static MongoClient mongoClient;
    public static DB db ;
    static Config config = ConfigFactory.load("db");

    public static DBCollection connectDB() {

        try {
            mongoClient = new MongoClient( config.getString("mongo.host") , config.getInt("mongo.port"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DB db = mongoClient.getDB(config.getString("mongo.database"));
        DBCollection coll = db.getCollection(config.getString("mongo.usersCollection"));
        return coll;
    }

    public static User insertIntoUser(User user) {
        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().
        append("username", user.username).
        append("email", user.email).
        append("password", user.password).
        append("profile", user.profile).
        append("latchAccountId", user.latchAccountId);

        coll.insert(WriteConcern.SAFE,query);

        mongoClient.close();
        return user;
    }

    public static void updateLatchAccountId(String username, String latchAccountId) {
        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().append("username", username);
        DBObject one = coll.findOne(query);

        if(one!=null) {
            BasicDBObject updateQuery1 = new BasicDBObject().append("$set", new BasicDBObject().append("latchAccountId", latchAccountId));
            coll.update(query, updateQuery1);
        }

        mongoClient.close();
    }

    public static User getUser(String username) {

        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().append("username", username);
        DBObject one = coll.findOne(query);

        if (one != null) {
            Logger.debug("Consulta no es null");
            mongoClient.close();
            return new User(String.valueOf(one.get("username")),
                    String.valueOf(one.get("email")),
                    String.valueOf(one.get("password")),
                    (User.Profile) one.get("profile"),
                    String.valueOf(one.get("latchAccountId")));
        }

        return null;
    }

}
