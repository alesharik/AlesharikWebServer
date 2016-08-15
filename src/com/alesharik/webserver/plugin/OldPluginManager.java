package com.alesharik.webserver.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

public class OldPluginManager {
    private File pluginsFolder;
    private ArrayList<PluginCore> plugins = new ArrayList<>();

    public OldPluginManager(File root) {
        this.pluginsFolder = new File(root.getPath() + "/plugins");
        if(!pluginsFolder.exists()) {
            pluginsFolder.mkdir();
        }
    }

    public void loadPlugins() {
        ArrayList<File> pluginFolders = Arrays.asList(pluginsFolder.listFiles()).stream()
                .filter(file -> file.isDirectory())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        pluginFolders.forEach(folder -> {
            try {
                URL url = folder.toURI().toURL();
                URL[] urls = new URL[]{url};
                ClassLoader cl = new URLClassLoader(urls);
                Class clazz = cl.loadClass("Main");
                plugins.add((PluginCore) clazz.newInstance());
            } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public void test() {
//        plugins.forEach(pluginCore -> pluginCore.test());
    }

//    public RequestHandler getCommandProvider() {
//        return plugins.get(0).getRequestHandler();
//    }


}
