package com.alesharik.webserver.control;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.collections.ConcurrentLiveArrayList;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.control.websockets.control.WebSocketController;
import com.alesharik.webserver.server.api.RequestHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.CharConversionException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This {@link RequestHandler} used for handle specific requests in control mode
 */
@SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
public final class ControlRequestHandlerOld implements RequestHandler {
    private final ConcurrentLiveArrayList<UUID> sessions = new ConcurrentLiveArrayList<>();
    private final FileManager fileManager;
    private final AdminDataStorageImpl adminDataStorageImpl;
    private final ConcurrentHashMap<String, WebSocketController> servers = new ConcurrentHashMap<>();

    private ServerConsoleCommandHandler serverConsoleCommandHandler;
    private ServerHolder serverHolder;

    private volatile ArrayList<byte[]> receivedBytes = new ArrayList<>();
    private volatile String folderName = "";

    public ControlRequestHandlerOld(FileManager fileManager, File rootFolder, AdminDataStorageImpl adminDataStorageImpl) {
        this.fileManager = fileManager;
        this.adminDataStorageImpl = adminDataStorageImpl;
    }

    @Override
    public boolean canHandleRequest(Request request) throws CharConversionException {
        String command = request.getDecodedRequestURI();
        return command.equals("/command") || command.contains("/login")
                || command.equals("/setValue") || command.equals("/getValue") || command.equals("/deleteValue")
                || command.equals("/changePassword") || command.equals("/addServer") || command.equals("/getServerBaseInfo")
                || command.equals("/isServerConnected") || command.equals("/deleteServer") || command.equals("/remoteServerCommand")
                || command.equals("/downloadPlugin") || command.equals("/sendFilePart");
    }

    @Override
    public void handleRequest(Request request, Response response) throws Exception {
        if(request.getDecodedRequestURI().equals("/command")) {
            serverConsoleCommandHandler.handleCommand(request, response);
            return;
        } else if(request.getDecodedRequestURI().equals("/login")) {
            String loginForm = request.getParameter("loginForm");
            String passwordForm = request.getParameter("passwordForm");
            if(adminDataStorageImpl.check(loginForm, passwordForm)) {
                response.addHeader(Header.Location, "com.alesharik.webserver.tests.html");
                UUID uuid = UUID.randomUUID();
                Cookie cookie = new Cookie("uuid", uuid.toString());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(3600);
                sessions.add(uuid, 3600L);
                response.addCookie(cookie);
            } else {
                response.addHeader(Header.Location, "index.html?incorrect=true");
            }
            response.setStatus(HttpStatus.FOUND_302);
            return;
        } else if(request.getDecodedRequestURI().equals("/getValue")) {
            if(hasValidUUID(request)) {
                String result = (String) adminDataStorageImpl.get(request.getParameter("key"));
                if(result == null) {
                    result = "";
                }
                response.setContentType(MIMETypes.findType(".txt"));
                response.setContentLength(result.length());
                response.getWriter().write(result);
                return;
            }
        } else if(request.getDecodedRequestURI().equals("/setValue")) {
            if(hasValidUUID(request)) {
                adminDataStorageImpl.put(request.getParameter("key"), request.getParameter("value"));
                return;
            }
        } else if(request.getDecodedRequestURI().equals("/deleteValue")) {
            if(hasValidUUID(request)) {
                adminDataStorageImpl.remove(request.getParameter("key"));
                return;
            }
        } else if(request.getDecodedRequestURI().equals("/changePassword")) {
            if(hasValidUUID(request)) {
                adminDataStorageImpl.updateLoginPassword(request.getParameter("oldLogin"), request.getParameter("oldPassword"), request.getParameter("newLogin"), request.getParameter("newPassword"));
                response.setContentType(MIMETypes.findType(".txt"));
                response.setContentLength("OK".length());
                response.getWriter().write("OK");
            } else {
                response.setStatus(HttpStatus.FORBIDDEN_403);
            }
        } else if(request.getDecodedRequestURI().equals("/addServer")) {
            if(hasValidUUID(request)) {
                String server = request.getParameter("server");
                serverHolder.addServer(server, Integer.parseInt(request.getParameter("serverIndex")));
//                servers.put(server, serverHolder.connectAndSend(server, request.getParameter("serverLogin"), request.getParameter("serverPassword")));
            }
            return;
        } else if(request.getDecodedRequestURI().equals("/getServerBaseInfo")) {
            if(hasValidUUID(request)) {
                WebSocketController endpoint = servers.get(serverHolder.getServer(Integer.parseInt(request.getParameter("serverIndex"))));
                String responsee = endpoint.sendMessageAndGetResponse("getBaseInfo");
                response.setContentLength(responsee.length());
                response.setContentType(MIMETypes.findType(".txt"));
                response.getWriter().write(responsee);
                return;
            }
        } else if(request.getDecodedRequestURI().equals("/isServerConnected")) {
            if(hasValidUUID(request)) {
                response.setContentType(MIMETypes.findType(".txt"));
                String serverIndex = request.getParameter("serverIndex");
                int index = Integer.parseInt(serverIndex);
                boolean exists = serverHolder.exists(index);
                if(exists) {
                    response.setContentLength("true".length());
                    response.getWriter().write("true");
                } else {
                    response.setContentLength("false".length());
                    response.getWriter().write("false");
                }
            }
        } else if(request.getDecodedRequestURI().equals("/deleteServer")) {
            if(hasValidUUID(request)) {
                serverHolder.removeServer(Integer.parseInt(request.getParameter("serverIndex")));
            }
        } else if(request.getDecodedRequestURI().equals("/remoteServerCommand")) {
            if(hasValidUUID(request)) {
                WebSocketController websocket = servers.get(serverHolder.getServer(Integer.parseInt(request.getParameter("serverIndex"))));
                String responsee = websocket.sendMessageAndGetResponse("Command=" + request.getParameter("command") + "=Params=" + request.getParameter("params"));
                response.setContentType(MIMETypes.findType(".txt"));
                response.setContentLength(responsee.length());
                response.getWriter().write(responsee);
                return;
            }
        } else if(request.getDecodedRequestURI().equals("/downloadPlugin")) {
            if(hasValidUUID(request)) {
                String data = request.getParameter("data").substring(request.getParameter("data").indexOf("data:;base64,"));
                new File(this.fileManager.getRootFolder().getParent() + "/modules/" + request.getParameter("folderName")).mkdir();
                File file = new File(this.fileManager.getRootFolder().getParent() + "/modules/" + request.getParameter("folderName") + "/Main.class");
                file.createNewFile();
                try (FileOutputStream fileOutputStream = new FileOutputStream(file);) {
                    fileOutputStream.write(Base64Utils.decode(data));
                    fileOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if(request.getDecodedRequestURI().equals("/sendFilePart")) {
            if(hasValidUUID(request)) {
                String data = request.getParameter("data").substring(13);
                folderName = request.getParameter("folderName");
                if(request.getParameter("index").equals("0")) {
                    receivedBytes = new ArrayList<>();
                }
                receivedBytes.add(Base64Utils.decode(data));
                if(request.getParameter("index").equals(request.getParameter("max"))) {
                    closeReceive();
                }
                System.out.println(request.getParameter("index") + "-" + request.getParameter("max"));
            }
        }
    }

    private void closeReceive() throws Exception {
        new File(this.fileManager.getRootFolder().getParent() + "/modules/" + folderName).mkdir();
        File file = new File(this.fileManager.getRootFolder().getParent() + "/modules/" + folderName + "/Main.class");
        file.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            receivedBytes.forEach(bytes -> {
                try {
                    fileOutputStream.write(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasValidUUID(Request request) {
        for(Cookie cookie : request.getCookies()) {
            if(cookie.getName().equals("uuid") && sessions.contains(UUID.fromString(cookie.getValue()))) {
                return true;
            }
        }
        return false;
    }
}
