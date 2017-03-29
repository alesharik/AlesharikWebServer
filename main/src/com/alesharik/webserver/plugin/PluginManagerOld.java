package com.alesharik.webserver.plugin;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import org.glassfish.grizzly.utils.Charsets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
@Prefix("[PluginManagerOld]")
public class PluginManagerOld {
    private final ArrayList<File> packages = new ArrayList<>();
    private final HashMap<File, MetaFile> packageMetaFiles = new HashMap<>();
    private final ArrayList<Class<?>> cores = new ArrayList<>();
    private final HashMap<String, MetaFile> metaFiles = new HashMap<>();
    private final AccessManagerBuilder accessManagerBuilder;

    private PluginHolderPool pluginHolderPool;
    private boolean isLoaded = false;

    public PluginManagerOld(AccessManagerBuilder accessManagerBuilder) {
        this.accessManagerBuilder = accessManagerBuilder;
        this.pluginHolderPool = new PluginHolderPool();
        isLoaded = true;
    }

    public void addPlugin(File pluginFolder) {
        if(isLoaded) {
            try {
                loadPlugin(pluginFolder);
            } catch (NoSuchMethodException | InvocationTargetException e) {
                Logger.log(e);
            }
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
        if(!isLoaded) {
            throw new IllegalStateException("Can't run manager without loading plugins!");
        }


//        ArrayList<PluginCore> cores = new ArrayList<>();
//        this.cores.forEach(core -> tryInitCore(cores, core));
//        cores.forEach(pluginHolderPool::addPlugin);
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
        return MetaFile.parse(new String(fileBytes, Charsets.UTF8_CHARSET));
    }

    private void loadPlugin(File pluginFolder) throws NoSuchMethodException, InvocationTargetException {
        try {
            MetaFile metaFile = loadMetaFile(pluginFolder);
            packageMetaFiles.put(pluginFolder, metaFile);
            metaFiles.put(metaFile.getAttribute("Name"), metaFile);
            ClassLoader classLoader = new URLClassLoader(new URL[]{pluginFolder.toURI().toURL()});
            Class<?> core = classLoader.loadClass(metaFile.getAttribute("Main-File"));
            if(pluginHolderPool != null) {
                Constructor<?> constructor = core.getDeclaredConstructor(AccessManager.class);
                constructor.setAccessible(true);
                pluginHolderPool.addPlugin((PluginCore) constructor.newInstance(accessManagerBuilder.forPermissions(metaFile.getAttribute("Access"), ",")));
            } else {
                cores.add(core);
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Logger.log("can't load plugin " + pluginFolder.getName() + ". Skipping...");
            Logger.log(e);
        }
    }

    private void tryInitCore(ArrayList<PluginCore> cores, Class<?> core) {
        try {
            cores.add((PluginCore) core.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
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
}