package com.alesharik.webserver.control;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.collections.LiveArrayList;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.control.dataHolding.AdminDataHolder;
import com.alesharik.webserver.main.FileManager;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.UUID;

public class ControlRequestHandler implements RequestHandler {
    private final LiveArrayList<UUID> sessions = new LiveArrayList<>();
    private final AdminDataHolder holder;
    private final FileManager fileManager;

    public ControlRequestHandler(FileManager fileManager, AdminDataHolder holder) {
        this.holder = holder;
        this.fileManager = fileManager;
    }

    @Override
    public boolean canHandleRequest(Request request) throws IOException {
        String command = request.getDecodedRequestURI();
        return command.equals("/login");
    }

    @Override
    public void handleRequest(Request request, Response response) throws Exception {
        String command = request.getDecodedRequestURI();
        switch (command) {
            case "/login":
                handleLoginCommand(request, response);
                break;
        }
    }

    public boolean isSessionValid(UUID sessionID) {
        return sessions.contains(sessionID);
    }

    private void handleLoginCommand(Request request, Response response) {
        String logpass = request.getParameter("logpass");
        boolean remember = Boolean.parseBoolean(request.getParameter("remember"));
        if(logpass != null) {
            if(holder.check(logpass)) {
                loginSuccess(response, remember, logpass);
                return;
            }
        }

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if(login == null || password == null) {
            loginFailed(response);
            return;
        }
        String loginPassword = LoginPasswordCoder.encode(login, password);
        if(holder.check(loginPassword)) {
            loginSuccess(response, remember, loginPassword);
        } else {
            loginFailed(response);
        }
    }

    private void loginSuccess(Response response, boolean remember, String logpass) {
        response.setStatus(HttpStatus.FOUND_302);
        response.addHeader(Header.Location, "/dashboard.html");
        UUID uuid = UUID.randomUUID();
        Cookie uuidCookie = new Cookie("UUID", uuid.toString());
        uuidCookie.setHttpOnly(true);
        uuidCookie.setPath("/");
        uuidCookie.setMaxAge(3600);
        sessions.add(uuid, 3600L);
        response.addCookie(uuidCookie);
        if(remember) {
            Cookie logpassCookie = new Cookie("Logpass", logpass);
            logpassCookie.setHttpOnly(false);
            logpassCookie.setPath("/");
            logpassCookie.setMaxAge(120);
            response.addCookie(logpassCookie);
        }
    }

    private void loginFailed(Response response) {
        response.setStatus(HttpStatus.FOUND_302);
        response.addHeader(Header.Location, "/index.html?incorrect=true");
    }
}
