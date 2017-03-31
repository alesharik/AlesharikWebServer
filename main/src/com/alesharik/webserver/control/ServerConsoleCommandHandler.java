package com.alesharik.webserver.control;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.control.commands.CdCommand;
import com.alesharik.webserver.control.commands.DirCommand;
import com.alesharik.webserver.control.commands.LsCommand;
import com.alesharik.webserver.control.commands.ParentCommand;
import com.alesharik.webserver.control.commands.TestCommand;
import com.alesharik.webserver.control.commands.ViewCommand;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.File;
import java.util.HashMap;


@Deprecated
public class ServerConsoleCommandHandler {
    private HashMap<String, IServerConsoleCommand> commands = new HashMap<>();
    File rootFolder;
    private HashMap<String, String> temporalyStore = new HashMap<>();

    public ServerConsoleCommandHandler(File root) {
        this.rootFolder = root;

        addCommand("test", new TestCommand());
        addCommand("ls", new LsCommand());
        addCommand("cd", new CdCommand());
        addCommand("dir", new DirCommand());
        addCommand("parent", new ParentCommand());
        addCommand("view", new ViewCommand());
    }

    public void addCommand(String command, IServerConsoleCommand consoleCommand) {
        commands.put(command, consoleCommand);
    }

    public void handleCommand(Request request, Response response) throws Exception {
        if(!commands.containsKey(request.getParameter("command"))) {
            response.setContentType(MIMETypes.findType(".txt"));
            response.setContentLength("Command not found".length());
            response.getWriter().write("Command not found");
            response.setStatus(HttpStatus.OK_200);
        } else {
            String result = commands.get(request.getParameter("command"))
                    .setRootFolder(this.rootFolder)
                    .setTemporalyStore(temporalyStore)
                    .handle(request.getParameter("parameters"));
            response.setContentType(MIMETypes.findType(".txt"));
            response.setContentLength(result.length());
            response.getWriter().write(result);
            response.setStatus(HttpStatus.OK_200);
        }
    }

    public String handle(String command, String parameters) {
        if(!commands.containsKey(command)) {
            return "Command not found";
        }
        return commands.get(command)
                .setRootFolder(this.rootFolder)
                .setTemporalyStore(this.temporalyStore)
                .handle(parameters);
    }
}

