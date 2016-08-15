package com.alesharik.webserver.main;

import com.alesharik.webserver.exceptions.FileFoundException;
import com.alesharik.webserver.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//TODO test check
public class FileManager {

    private static final String PREFIX = "[FileManager]";
    /**
     * This node contains root folder
     */
    private volatile FileNode mainNode;
    /**
     * Working folder
     */
    private final File rootFolder;
    private File logsFolder = null;

    private FileHoldingMode holdingMode;
    private FileChecker fileChecker = null;

    /*
    ===============================Params===============================
     */
    private boolean disableIgnoringFiles = false;
    private boolean hiddenFilesIgnored = false;
    private boolean isLogsIgnored = true;

    public FileManager(File rootFolder, FileHoldingMode mode) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        init();
    }

    public FileManager(File rootFolder, FileHoldingMode mode, FileHoldingParams... params) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        parseParameters(params);
        init();
    }

    private void init() {
        try {
            if(isLogsIgnored) {
                this.logsFolder = new File(rootFolder + "/logs");
            }
            this.mainNode = readFolder(this.rootFolder);
            Logger.log(PREFIX, "FileManager successfully initialized");

            if(holdingMode == FileHoldingMode.HOLD_AND_CHECK) {
                fileChecker = new FileChecker();
                fileChecker.start();
            }
        } catch (FileFoundException | FileNotFoundException e) {
            Logger.log(PREFIX, e);
            dumpState();
            throw new RuntimeException("Can't initialize FileManager!");
        }
    }

    private void parseParameters(FileHoldingParams[] parameters) {
        Arrays.asList(parameters).forEach(fileHoldingParameter -> {
            switch (fileHoldingParameter) {
                case DISABLE_IGNORING_FILES:
                    disableIgnoringFiles = true;
                    break;
                case IGNORE_HIDDEN_FILES:
                    hiddenFilesIgnored = true;
                    break;
                case DISABLE_IGNORE_LOGS_FOLDER:
                    isLogsIgnored = false;
                    break;
            }
        });
    }

    public void shutdown() {
        if(holdingMode == FileHoldingMode.HOLD_AND_CHECK) {
            fileChecker.shutdown();
        }
    }

    /**
     * Add file to holding if it exist
     */
    public void addFile(File file) throws FileNotFoundException {
        if(!file.exists() || !file.getPath().startsWith(rootFolder.getPath())) {
            throw new FileNotFoundException();
        }
        String[] parts = file.getPath().substring(rootFolder.getPath().length() + 1).split("/");
        FileNode node = findFileNodeFromParts(parts);
        FileHolder holder = new FileHolder(file, file.getName());
        if(!(holdingMode == FileHoldingMode.NO_HOLD)) {
            holder.read();
        }
        node.files.put(file.getName(), holder);
    }

    /**
     * Add folder and content to holding
     */
    public void addFolderWithFiles(File folder) throws FileNotFoundException, FileFoundException {
        if(!folder.exists() || !folder.getPath().startsWith(rootFolder.getPath())) {
            throw new FileNotFoundException();
        } else if(folder.isFile()) {
            throw new FileFoundException();
        }

        String[] parts = folder.getPath().substring(rootFolder.getPath().length() + 1).split("/");
        FileNode node = findFileNodeFromParts(parts);
        node.nodes.put(folder.getName(), readFolder(folder));
    }

    /**
     * Reload file's contents
     *
     * @param file dynamical path to file
     */
    public void reload(String file) throws FileNotFoundException, FileFoundException {
        if(file.contains(".")) {
            if(holdingMode != FileHoldingMode.NO_HOLD) {
                getFileFromPath(file).read();
            }
        } else {
            String[] parts = file.split("/");
            FileNode folder = findFileNodeFromParts(parts);
            FileNode newFolder = readFolder(new File(rootFolder + (file.startsWith("/") ? file : "/" + file)));
            folder.nodes = newFolder.nodes;
            folder.files = newFolder.files;
            if(!folder.name.equals(newFolder.name)) {
                throw new RuntimeException("This is never happens!");
            }
        }
    }

    /**
     * Delete file/folder from holding. If need to delete folder from holding, this delete file and all contents form
     * holding
     */
    public void removeFileFromHold(String file) {
        if(file.startsWith("/")) {
            file = file.substring(1);
        }
        String[] parts = file.split("/");
        FileNode folder = findFileNodeFromParts(parts);
        String fileName = parts[parts.length - 1];
        if(fileName.contains(".")) {
            folder.files.remove(fileName);
        } else {
            folder.nodes.remove(fileName);
        }
    }

    /**
     * Read file and return it's content. If file was not found return byte[0]
     */
    public byte[] readFile(String file) {
        if(holdingMode == FileHoldingMode.NO_HOLD) {
            return getFileFromPath(file).getFileContentFromRealFile();
        } else {
            return getFileFromPath(file).data;
        }
    }

    /**
     * Write/replace content in file
     */
    public void writeToFile(String file, byte[] data) {
        if(holdingMode == FileHoldingMode.NO_HOLD) {
            getFileFromPath(file).writeToRealFile(data);
        } else {
            FileHolder fileHolder = getFileFromPath(file);
            fileHolder.data = data;
            fileHolder.write();
        }
    }

    /**
     * Return true if file/folder exists in holding
     */
    public boolean exists(String file) {
        if(file.contains(".")) {
            return getFileFromPath(file) != null;
        } else {
            if(file.startsWith("/")) {
                file = file.substring(1);
            }
            String[] parts = file.split("/");
            return findFileNodeFromParts(parts) != null;
        }
    }

    /**
     * Create file and add it to hold
     */
    public void create(String file) throws IOException {
        File realFile = new File(rootFolder + (file.startsWith("/") ? file : "/" + file));
        if(realFile.createNewFile()) {
            addFile(realFile);
        }
    }

    /**
     * Create folder with <code>File.mkdir()</code>
     */
    public void createFolder(String folder) throws IOException {
        File realFolder = new File(rootFolder + (folder.startsWith("/") ? folder : "/" + folder));
        if(realFolder.mkdir()) {
            addFile(realFolder);
        }
    }

    /**
     * Delete file/folder form hold and from disk
     */
    public void realDelete(String file) {
        if(file.contains(".")) {
            FileHolder holder = getFileFromPath(file);
            holder.file.delete();
            removeFileFromHold(file);
        } else {
            new File(rootFolder + (file.contains("/") ? file : "/" + file)).delete();
            removeFileFromHold(file);
        }
    }

    /**
     * return {@link ArrayList} of folders and files in selected node
     */
    public ArrayList<String> getFolderContents(String folder) {
        ArrayList<String> ret = new ArrayList<>();
        if(folder.startsWith("/")) {
            folder = folder.substring(1);
        }
        String[] parts = folder.split("/");
        FileNode fileNode = findFileNodeFromParts(parts);
        fileNode.nodes.values().forEach(node -> ret.add(node.name));
        fileNode.files.values().forEach(fileHolder -> ret.add(fileHolder.name));
        return ret;
    }

    public void dumpState() {
        Logger.log(PREFIX, "Root folder: " + rootFolder);
        Logger.log(PREFIX, "Logs folder: " + logsFolder);
        Logger.log(PREFIX, "Holding mode: " + holdingMode);
        Logger.log(PREFIX, "disableIgnoringFiles=" + disableIgnoringFiles);
        Logger.log(PREFIX, "hiddenFilesIgnored=" + hiddenFilesIgnored);
        Logger.log(PREFIX, "ignoreLogsFolder=" + isLogsIgnored);
    }

    private FileNode readFolder(File folder) throws FileFoundException, FileNotFoundException {
        if(!folder.exists()) {
            throw new FileNotFoundException();
        } else if(folder.isFile()) {
            throw new FileFoundException();
        } else if(folder.isHidden() && hiddenFilesIgnored) {
            return new FileNode();
        }

        if(folder.listFiles() == null) {
            return new FileNode(folder.getName());
        }

        ArrayList<File> ignoredFiles = new ArrayList<>();
        if(folder.equals(rootFolder) && isLogsIgnored) {
            ignoredFiles.add(logsFolder);
        }

        if(!disableIgnoringFiles) {
            File configFile = new File(folder.getPath() + "/ignoredFiles.conf");
            if(configFile.exists() && configFile.isFile()) {
                try (BufferedReader reader = Files.newBufferedReader(configFile.toPath())) {
                    String line;
                    while((line = reader.readLine()) != null) {
                        File file = new File(folder.getPath() + line);
                        if(file.exists()) {
                            ignoredFiles.add(file);
                        }
                    }
                } catch (IOException e) {
                    Logger.log(e);
                }
            }
        }

        List<File> folderContent = Arrays.asList(folder.listFiles()).stream().collect(Collectors.toCollection(ArrayList::new));
        HashMap<String, FileHolder> files = new HashMap<>();
        folderContent.parallelStream()
                .filter(File::isFile)
                .filter(file -> !ignoredFiles.contains(file))
                .filter(file -> !hiddenFilesIgnored || !file.isHidden())
                .forEach(file -> {
                    FileHolder holder = new FileHolder(file, file.getName());
                    if(holdingMode != FileHoldingMode.NO_HOLD) {
                        holder.read();
                    }
                    files.put(file.getName(), holder);
                });
        HashMap<String, FileNode> folders = new HashMap<>();
        folderContent.parallelStream()
                .filter(File::isDirectory)
                .filter(file -> !ignoredFiles.contains(file))
                .filter(file -> !hiddenFilesIgnored || !file.isHidden())
                .forEach(file -> {
                    try {
                        FileNode node = readFolder(file);
                        folders.put(file.getName(), node);
                    } catch (FileNotFoundException | FileFoundException e) {
                        Logger.log(PREFIX, "Can't read folder " + file + " because " + e.getLocalizedMessage());
                        Logger.log(e);
                    }
                });

        return new FileNode(folder.getName(), files, folders);
    }

    private FileHolder getFileFromPath(String path) {
        try {
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
            String[] parts = path.split("/");
            FileNode folder = findFileNodeFromParts(parts);
            String fileName = parts[parts.length - 1];
            return folder.files.get(fileName);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private FileNode findFileNodeFromParts(String[] parts) {
        FileNode node = mainNode;
        if(parts.length > 1) {
            for(int i = 0; i < parts.length - 1; i++) {
                node = node.nodes.get(parts[i]);
            }
        }
        return node;
    }

    private class FileChecker extends Thread {
        private boolean isRunning = true;

        public FileChecker() {
            this.setName("FileChecker");
        }

        public void run() {
            WatchService watcher = null;
            try {
                watcher = FileSystems.getDefault().newWatchService();
                rootFolder.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                Logger.log(e);
            }
            try {
                while(isRunning) {
                    final WatchKey key = watcher.poll(10, TimeUnit.SECONDS);
                    if(key != null) {
                        key.pollEvents().forEach(watchEvent -> {
                            try {
                                final WatchEvent.Kind<?> kind = watchEvent.kind();
                                File realFile = new File(FileManager.this.rootFolder + "/" + ((WatchEvent<Path>) watchEvent).context().toFile().getPath());
                                File fileAddress = new File("/" + ((WatchEvent<Path>) watchEvent).context().toFile().getPath());
                                if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                                    if(realFile.isDirectory()) {
                                        FileManager.this.addFolderWithFiles(realFile);
                                    } else {
                                        FileManager.this.addFile(realFile);
                                    }
                                } else if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                    FileManager.this.reload(realFile.getPath().substring(FileManager.this.rootFolder.getPath().length()));
                                } else if(kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                                    FileManager.this.removeFileFromHold(realFile.getPath().substring(FileManager.this.rootFolder.getPath().length()));
                                }
                            } catch (FileNotFoundException | FileFoundException e) {
                                Logger.log(e);
                            }
                        });
                    }
                }
            } catch (InterruptedException e) {
                Logger.log(e);
            }
            try {
                watcher.close();
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        public void shutdown() {
            this.isRunning = false;
        }
    }

    private static class FileNode {
        /**
         * Files in the node
         */
        public ConcurrentHashMap<String, FileHolder> files;
        /**
         * Nodes in the node
         */
        public ConcurrentHashMap<String, FileNode> nodes;
        /**
         * Name of the node - name of the folder
         */
        public volatile String name;

        public FileNode(String name, HashMap<String, FileHolder> files, HashMap<String, FileNode> nodes) {
            this.name = name;
            this.files = new ConcurrentHashMap<>(files);
            this.nodes = new ConcurrentHashMap<>(nodes);
        }

        public FileNode() {
        }

        public FileNode(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            FileNode fileNode = (FileNode) o;

            if(files != null ? !files.equals(fileNode.files) : fileNode.files != null) return false;
            if(nodes != null ? !nodes.equals(fileNode.nodes) : fileNode.nodes != null) return false;
            return name != null ? name.equals(fileNode.name) : fileNode.name == null;

        }

        @Override
        public int hashCode() {
            int result = files != null ? files.hashCode() : 0;
            result = 31 * result + (nodes != null ? nodes.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FileNode{" +
                    "files=" + files +
                    ", nodes=" + nodes +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    /**
     * This class hold file and it's data
     */
    private static class FileHolder {
        /**
         * Address of hold file
         */
        public File file;
        /**
         * Data of hold file
         */
        public byte[] data;
        /**
         * Name of real file
         */
        public String name;

        public FileHolder(File file, String name) {
            this.file = file;
            this.name = name;
        }

        /**
         * Read data from file
         */
        public void read() {
            try {
                this.data = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                Logger.log("Error in reading file " + file);
                Logger.log(e);
            } catch (SecurityException e) {
                Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
            }
        }

        /**
         * Write data to file
         */
        public void write() {
            try {
                Files.write(file.toPath(), data);
            } catch (IOException e) {
                Logger.log("Error in writing file " + file);
            } catch (SecurityException e) {
                Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
            }
        }

        /**
         * Check is file exists
         */
        public boolean exists() {
            return file.exists();
        }

        /**
         * Read real file and return its content
         */
        public byte[] getFileContentFromRealFile() {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                Logger.log("Error in reading file " + file);
                Logger.log(e);
            } catch (SecurityException e) {
                Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
            }
            return new byte[0];
        }

        public void writeToRealFile(byte[] data) {
            try {
                Files.write(file.toPath(), data);
            } catch (IOException e) {
                Logger.log("Error in writing file " + file);
            } catch (SecurityException e) {
                Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
            }
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            FileHolder that = (FileHolder) o;

            if(file != null ? !file.equals(that.file) : that.file != null) return false;
            if(!Arrays.equals(data, that.data)) return false;
            return name != null ? name.equals(that.name) : that.name == null;

        }

        @Override
        public int hashCode() {
            int result = file != null ? file.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(data);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FileHolder{" +
                    "file=" + file +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public File getRootFolder() {
        return rootFolder;
    }

    public enum FileHoldingMode {
        /**
         * Don't hold files in memory
         */
        NO_HOLD,
        /**
         * Simply hold files in memory
         */
        HOLD,
        /**
         * Hold files in memory and check for files updates
         */
        HOLD_AND_CHECK
    }

    public enum FileHoldingParams {
        /**
         * Disable reading and ignore ignoredFiles.json
         */
        DISABLE_IGNORING_FILES,
        /**
         * Enable ignoring all of the hidden files and folders
         */
        IGNORE_HIDDEN_FILES,
        /**
         * Disable ignoring /logs
         */
        DISABLE_IGNORE_LOGS_FOLDER
    }
}
