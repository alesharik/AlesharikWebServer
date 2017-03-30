package com.alesharik.webserver.server;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.server.api.WSApplication;
import com.alesharik.webserver.server.api.WSChecker;
import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class register and holds all websocket applications
 */
@ClassPathScanner
@UtilityClass
@Prefix("[WebSocketApplicationManager]")
public class WebSocketApplicationManager {
    private static final Set<WebSocketApplication> applications = new CopyOnWriteArraySet<>();

    @ListenAnnotation(WSApplication.class)
    public static void listenWebsocketApplication(Class<?> clazz) {
        if(WebSocketApplication.class.isAssignableFrom(clazz)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                constructor.setAccessible(true);
                WebSocketApplication app = (WebSocketApplication) constructor.newInstance();
                applications.add(app);

                WSApplication application = clazz.getAnnotation(WSApplication.class);
                Class<?> checker = application.checker();
                if(!WSChecker.class.isAssignableFrom(checker)) {
                    checker = WSChecker.class;
                    System.out.println("Checker of class " + clazz.toString() + " must extend WSChecker");
                }
                WSChecker wsChecker = (WSChecker) checker.newInstance();
                if(wsChecker.enabled()) {
//                    WebSocketEngine.getEngine().register(application.contextPath(), application.value(), app); //TODO rewrite
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Class " + clazz.toString() + " use WSApplication annotation but not extend WebSocketApplication class");
        }
    }

    public static Set<WebSocketApplication> getApplications() {
        return Collections.unmodifiableSet(applications);
    }
}
