import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.Handler;
import play.libs.F;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Results;
import play.mvc.SimpleResult;

import java.lang.reflect.Method;

/**
 * Created by Nerea on 15/10/2014.
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application application) {
        super.onStart(application);
    }

    @Override
    public void onStop(Application application) {
        super.onStop(application);
    }

    @Override
    public F.Promise<SimpleResult> onError(Http.RequestHeader requestHeader, Throwable t) {

        Logger.error("###########################################################################");
        Logger.error("Gestión de Errores de Estanque");
        Logger.error("###########################################################################");

        if(true) {

            Logger.error("###########################################################################");
            Logger.error(String.format("Mensaje Excepción %s", t.getMessage()));
            Logger.error("###########################################################################");

            //StringWriter sw = new StringWriter();
            //PrintWriter pw = new PrintWriter(sw);
            //t.printStackTrace(pw);


            ObjectNode exceptioJSON = Json.newObject();
            exceptioJSON.put("Message", t.getMessage());
            exceptioJSON.put("LocalizedMessage", "Not Necessary");
            exceptioJSON.put("StackTrace", "Not Necessary");



            Logger.error("Fin de gestión Excepciones de cuaQea");
            Logger.error("###########################################################################");

            return F.Promise.<SimpleResult>pure(Results.badRequest(

                    exceptioJSON
            ));

        }else{

            Logger.error("###########################################################################");
            Logger.error("Gestión de Errores de Play");
            Logger.error("###########################################################################");

            return super.onError(requestHeader, t);
        }
    }

    @Override
    public Action onRequest(Http.Request request, Method method) {
        //TODO send to estanqe what user will do
        System.out.println("before each request... " + request.toString());
        System.out.println("request.body()... " + request.body().toString());
        return super.onRequest(request, method);
    }

    @Override
    public Handler onRouteRequest(Http.RequestHeader requestHeader) {
        return super.onRouteRequest(requestHeader);
    }
}
