package com.alesharik.webserver.main;

import com.alesharik.webserver.api.CompressionUtils;
import com.alesharik.webserver.exceptions.FileFoundException;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import one.nio.mem.OutOfMemoryException;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

@Prefix("[FileManager]")
public class FileManager {
    /**
     * 2 Gb
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 2147483648L;

    private final long maxFileSize;
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
    private boolean isCompressionEnabled = false;

    private int compressionLevel = -1;

    public FileManager(File rootFolder, FileHoldingMode mode) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        this.maxFileSize = DEFAULT_MAX_FILE_SIZE;
        check();
        init();
    }

    public FileManager(File rootFolder, FileHoldingMode mode, FileHoldingParams... params) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        this.maxFileSize = DEFAULT_MAX_FILE_SIZE;
        parseParameters(params);
        check();
        init();
    }

    public FileManager(File rootFolder, FileHoldingMode mode, long maxFileSize) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        this.maxFileSize = maxFileSize;
        check();
        init();
    }

    public FileManager(File rootFolder, FileHoldingMode mode, long maxFileSize, FileHoldingParams... params) {
        this.rootFolder = rootFolder;
        this.holdingMode = mode;
        this.maxFileSize = maxFileSize;
        parseParameters(params);
        check();
        init();
    }

    private void init() {
        try {
            if(isLogsIgnored) {
                this.logsFolder = new File(rootFolder + "/logs");
            }
            this.mainNode = readFolder(this.rootFolder);
            Logger.log("FileManager successfully initialized");

            if(holdingMode == FileHoldingMode.HOLD_AND_CHECK) {
                fileChecker = new FileChecker();
                fileChecker.start();
            }
        } catch (FileFoundException | FileNotFoundException e) {
            Logger.log(e);
            dumpState();
            throw new RuntimeException("Can't initialize FileManager!");
        }
    }

    private void check() {
        if(holdingMode != FileHoldingMode.NO_HOLD) {
            if(FileUtils.sizeOfDirectory(this.rootFolder) > Runtime.getRuntime().freeMemory()) {
                throw new OutOfMemoryException("Can't allocate memory on file holding!");
            }
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
                case ENABLE_COMPRESSION:
                    isCompressionEnabled = true;
                    if(fileHoldingParameter.getValue() != null) {
                        compressionLevel = ((Integer) fileHoldingParameter.getValue());
                    }
                    break;
            }
        });
    }

    /**
     * If you use HOLD_AND_CHECK mode, please, on program shutdown execute this method!
     */
    public void shutdown() {
        if(holdingMode == FileHoldingMode.HOLD_AND_CHECK) {
            fileChecker.shutdown();
        }
    }

    /**
     * Add file to holding if it exist
     */
    //FIXME
    public void addFile(File file) throws FileNotFoundException {
        if(!file.exists() || !file.getPath().startsWith(rootFolder.getPath())) {
            throw new FileNotFoundException();
        }
        String[] parts = file.getPath().substring(rootFolder.getPath().length() + 1).split("/");
        FileNode node = findFileNodeFromParts(parts);
        FileHolder holder;
        if(file.length() > maxFileSize || holdingMode == FileHoldingMode.NO_HOLD) {
            Logger.log("File " + file + " is too large!");
            holder = new FileHolder(file, file.getName(), FileHoldingMode.NO_HOLD);
        } else {
            holder = new FileHolder(file, file.getName(), holdingMode);
        }
        holder.check();
        holder.setCompression(isCompressionEnabled, compressionLevel);
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
        getFileFromPath(file).check();
    }

    /**
     * Delete file/folder from holding. If need to delete folder from holding, this delete file and all contents form
     * holding
     */
    public void removeFileFromHold(String file, boolean isFile) {
        if(file.startsWith("/")) {
            file = file.substring(1);
        }
        String[] parts = file.split("/");
        FileNode folder = findFileNodeFromParts(parts);
        String fileName = parts[parts.length - 1];
        if(isFile) {
            folder.files.remove(fileName);
        } else {
            folder.nodes.remove(fileName);
        }
    }

    /**
     * Read file and return it's content. If file was not found return byte[0]
     */
    public byte[] readFile(String file) {
        return getFileFromPath(file).read();
    }

    /**
     * Write/replace content in file
     */
    public void writeToFile(String file, byte[] data) {
        FileHolder fileHolder = getFileFromPath(file);
        fileHolder.write(data);
    }

    /**
     * Return true if file/folder exists in holding
     */
    public boolean exists(String file, boolean isFile) {
        if(isFile) {
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
    public void realDelete(String file, boolean isFile) {
        if(isFile) {
            FileHolder holder = getFileFromPath(file);
            if(holder.file.delete()) {
                removeFileFromHold(file, true);
            }
        } else {
            if(!new File(rootFolder + (file.contains("/") ? file : "/" + file)).delete()) {
                Logger.log("Can't delete file " + file);
            }
            removeFileFromHold(file, false);
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
        Logger.log("Root folder: " + rootFolder);
        Logger.log("Logs folder: " + logsFolder);
        Logger.log("Holding mode: " + holdingMode);
        Logger.log("disableIgnoringFiles=" + disableIgnoringFiles);
        Logger.log("hiddenFilesIgnored=" + hiddenFilesIgnored);
        Logger.log("ignoreLogsFolder=" + isLogsIgnored);
    }

    private FileNode readFolder(File folder) throws FileFoundException, FileNotFoundException {
        if(!folder.exists()) {
            throw new FileNotFoundException();
        } else if(folder.isFile()) {
            throw new FileFoundException();
        } else if(folder.isHidden() && hiddenFilesIgnored) {
            return new FileNode();
        }

        final File[] listFiles = folder.listFiles();
        if(listFiles == null) {
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

        List<File> folderContent = Arrays.stream(listFiles).collect(Collectors.toCollection(ArrayList::new));
        HashMap<String, FileHolder> files = new HashMap<>();
        folderContent.parallelStream()
                .filter(File::isFile)
                .filter(file -> !ignoredFiles.contains(file))
                .filter(file -> !hiddenFilesIgnored || !file.isHidden())
                .forEach(file -> {
                    FileHolder holder;
                    if(file.length() > maxFileSize) {
                        Logger.log("File " + file + " is too large!");
                        holder = new FileHolder(file, file.getName(), FileHoldingMode.NO_HOLD);
                    } else {
                        holder = new FileHolder(file, file.getName(), holdingMode);
                    }
                    holder.check();
                    holder.setCompression(isCompressionEnabled, compressionLevel);
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
                        Logger.log("Can't read folder " + file + " because " + e.getLocalizedMessage());
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
            return FileHolder.EMPTY_FILE_HOLDER;
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

    @Prefix("[FileChecker]")
    private class FileChecker extends Thread {
        private boolean isRunning = true;

        public FileChecker() {
            this.setName("FileChecker");
        }

        public void run() {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        dir.register(
                                watcher,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE);

                        return FileVisitResult.CONTINUE;
                    }
                };
                Files.walkFileTree(rootFolder.toPath(), fileVisitor);


                while(isRunning) {
                    mainLoop(watcher);
                }

                watcher.close();
            } catch (InterruptedException | IOException e) {
                Logger.log(e);
            }
        }

        private void mainLoop(WatchService watcher) throws InterruptedException {
            processWatchKey(watcher.take());
        }

        private void processWatchKey(WatchKey key) {
            key.pollEvents().forEach(watchEvent -> {
                try {
                    final WatchEvent.Kind<?> kind = watchEvent.kind();
                    if(kind == StandardWatchEventKinds.OVERFLOW) {
                        Logger.log("Oops! We have a problem! Watcher is overflowed!");
                        return;
                    }

                    File realFile = new File(FileManager.this.rootFolder + "/" + ((WatchEvent<Path>) watchEvent).context().toFile().getPath());
                    if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if(realFile.isDirectory()) {
                            FileManager.this.addFolderWithFiles(realFile);
                        } else {
                            FileManager.this.addFile(realFile);
                        }
                    } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        FileManager.this.reload(realFile.getPath().substring(FileManager.this.rootFolder.getPath().length()));
                    } else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        FileManager.this.removeFileFromHold(realFile.getPath().substring(FileManager.this.rootFolder.getPath().length()), realFile.isFile());
                    }
                } catch (FileNotFoundException | FileFoundException e) {
                    Logger.log(e);
                }
            });

            key.reset();
            if(!key.isValid()) {
                shutdown();
            }
        }

        public void shutdown() {
            Logger.log("Shutting down FileChecker");
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
        public static final FileHolder EMPTY_FILE_HOLDER = new FileHolder(new File(""), "", FileHoldingMode.HOLD);

        private final FileHoldingMode mode;
        public final File file;
        public final String name;

        public byte[] data = new byte[0];

        private boolean isCompressionEnabled = false;
        private int compressionLevel;

        public FileHolder(File file, String name, FileHoldingMode mode) {
            this.file = file;
            this.name = name;
            this.mode = mode;
        }

        public void setCompression(boolean enabled, int level) {
            this.isCompressionEnabled = enabled;
            this.compressionLevel = level;
        }

        /**
         * Read data from file
         */
        public byte[] read() {
            try {
                if(mode == FileHoldingMode.NO_HOLD) {
                    return Files.readAllBytes(file.toPath());
                } else if(isCompressionEnabled) {
                    return CompressionUtils.decompress(data);
                } else {
                    return data;
                }
            } catch (IOException | DataFormatException e) {
                Logger.log("Error in reading file " + file);
                Logger.log(e);
            } catch (SecurityException e) {
                Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
            }
            return new byte[0];
        }

        /**
         * Write data to file
         */
        public void write(byte[] data) {
            try {
                if(mode != FileHoldingMode.NO_HOLD) {
                    if(isCompressionEnabled) {
                        this.data = CompressionUtils.compress(data, compressionLevel);
                    } else {
                        this.data = data;
                    }
                }
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

        public void check() {
            if(mode != FileHoldingMode.NO_HOLD) {
                try {
                    if(isCompressionEnabled) {
                        this.data = CompressionUtils.compress(Files.readAllBytes(file.toPath()), compressionLevel);
                    } else {
                        this.data = Files.readAllBytes(file.toPath());
                        ;
                    }
                } catch (IOException e) {
                    Logger.log("Error in reading file " + file);
                    Logger.log(e);
                } catch (SecurityException e) {
                    Logger.log("Can't read file " + file + " because " + e.getLocalizedMessage());
                }
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
                    "mode=" + mode +
                    ", file=" + file +
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
        DISABLE_IGNORE_LOGS_FOLDER,
        /**
         * Enable file compression. Use value to set compression level. Value must be {@link Integer}!
         */
        ENABLE_COMPRESSION;

        private Object value = null;

        FileHoldingParams() {
        }

        public FileHoldingParams setValue(Object value) {
            this.value = value;
            return this;
        }

        public Object getValue() {
            return value;
        }
    }
}
