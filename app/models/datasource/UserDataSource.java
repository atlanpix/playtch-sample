package models.datasource;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.SecurityController;
import models.entities.*;
import play.Logger;
import play.api.libs.json.JsPath;
import play.libs.Json;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Nerea on 15/10/2014.
 */
public class UserDataSource {

    public static MongoClient mongoClient;
    public static DB db ;
    static Config config = ConfigFactory.load("db");
    static Config configSecurity = ConfigFactory.load("application");

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

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, configSecurity.getInt("security.secretTokenExpirationLimit"));

        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().
        append("username", user.username).
        append("email", user.email).
        append("password", user.password).
        append("profile", JSON.parse(user.profile.toString())).
        append("latchAccountId", user.latchAccountId).
        append("secretToken", SecurityController.createSecretToken()).
        append("secretTokenExpiration", calendar.getTime());

        coll.insert(WriteConcern.SAFE,query);

        mongoClient.close();
        return user;
    }

    public static void updateLatchAccountId(String username, String latchAccountId) {
        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().append("username", username);
        DBObject one = coll.findOne(query);

        if(one!=null) {
            BasicDBObject updateQuery = new BasicDBObject().append("$set", new BasicDBObject().append("latchAccountId", latchAccountId));
            coll.update(query, updateQuery);
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
            String profile = one.get("profile").toString();
            Logger.debug("PROFILE JSON: "+profile);
            return new User(String.valueOf(one.get("username")),
                    String.valueOf(one.get("email")),
                    String.valueOf(one.get("password")),
                    new Gson().fromJson(profile , User.Profile.class),
                    String.valueOf(one.get("latchAccountId")),
                    String.valueOf(one.get("secretToken")),
                    (Date) one.get("secretTokenExpirationDate"));
        }

        return null;
    }

    public static void updateSecretToken(String username){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, configSecurity.getInt("security.secretTokenExpirationLimit"));

        DBCollection coll = connectDB();
        BasicDBObject query = new BasicDBObject().append("username", username);
        DBObject one = coll.findOne(query);

        if(one!=null) {
            BasicDBObject updateQuery = new BasicDBObject().append("$set", new BasicDBObject().
                    append("secretToken", SecurityController.createSecretToken()).
                    append("secretTokenExpiration", calendar.getTime()));
            coll.update(query, updateQuery);
        }

        mongoClient.close();
    }
}
