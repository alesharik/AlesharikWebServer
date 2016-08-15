package com.alesharik.webserver.plugin;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

@Prefix("[PluginManager]")
public class PluginManager {
    private final PluginAccessManager accessManager;
    private final ArrayList<File> packages = new ArrayList<>();
    private final HashMap<File, MetaFile> packageMetaFiles = new HashMap<>();
    private final ArrayList<Class<?>> cores = new ArrayList<>();
    private final HashMap<String, MetaFile> metaFiles = new HashMap<>();
    private PluginHolderPool pluginHolderPool;
    private boolean isLoaded = false;

    PluginManager(PluginAccessManager accessManager) {
        this.accessManager = accessManager;
    }

    public void addPlugin(File pluginFolder) {
        if(isLoaded) {
            loadPlugin(pluginFolder);
        } else {
            packages.add(pluginFolder);
        }
    }

    public void loadPlugins() {
        loadConfigFiles();
        loadPluginCores();

        isLoaded = true;
    }

    public void start() {
        this.pluginHolderPool = new PluginHolderPool(accessManager);

        ArrayList<PluginCore> cores = new ArrayList<>();
        this.cores.forEach(core -> tryInitCore(cores, core));
        cores.forEach(core -> pluginHolderPool.addPlugin(core, metaFiles.get(core.getName())));
    }

    private void tryInitCore(ArrayList<PluginCore> cores, Class<?> core) {
        try {
            cores.add((PluginCore) core.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.log(e);
        }
    }

    private void loadPlugin(File pluginFolder) {
        try {
            MetaFile metaFile = loadMetaFile(pluginFolder);
            packageMetaFiles.put(pluginFolder, metaFile);
            metaFiles.put(metaFile.getAttribute("Name"), metaFile);
            ClassLoader classLoader = new URLClassLoader(new URL[]{pluginFolder.toURI().toURL()});
            Class<?> core = classLoader.loadClass(metaFile.getAttribute("Main-File"));
            if(pluginHolderPool != null) {
                pluginHolderPool.addPlugin((PluginCore) core.newInstance(), metaFile);
            } else {
                cores.add(core);
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Logger.log("can't load plugin " + pluginFolder.getName() + ". Skipping...");
            Logger.log(e);
        }
    }

    private void loadPluginCores() {
        ArrayList<URL> urls = new ArrayList<>();
        ArrayList<String> coresAddresses = new ArrayList<>();
        packageMetaFiles.forEach((file, metaFile) -> {
            try {
                urls.add(file.toURI().toURL());
                coresAddresses.add(metaFile.getAttribute("Main-File"));
                metaFiles.put(metaFile.getAttribute("Name"), metaFile);
            } catch (MalformedURLException e) {
                Logger.log("Can't create url of file " + file);
                Logger.log(e);
            }
        });
        ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
        coresAddresses.forEach(address -> {
            tryLoadCore(classLoader, address);
        });
    }

    private void tryLoadCore(ClassLoader classLoader, String address) {
        try {
            cores.add(classLoader.loadClass(address));
        } catch (ClassNotFoundException e) {
            Logger.log(e);
        }
    }

    private void loadConfigFiles() {
        packages.forEach(file -> {
            File config = new File(file.getPath() + "/MAIN.META");
            if(!config.exists()) {
                Logger.log("Meta file of plugin  " + file.getName() + " not exists. Skipping...");
            } else {
                tryLoadMetaFile(file);
            }
        });
    }

    private void tryLoadMetaFile(File file) {
        try {
            packageMetaFiles.put(file, loadMetaFile(file));
        } catch (IOException e) {
            Logger.log("can't load plugin " + file.getName() + ". Skipping...");
            Logger.log(e);
        }
    }

    private MetaFile loadMetaFile(File file) throws IOException {
        final byte[] fileBytes = Files.readAllBytes(new File(file.getPath() + "/MAIN.META").toPath());
        return MetaFile.parse(new String(fileBytes));
    }
}
