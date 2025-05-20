import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The FileWatcher monitors a directory using Java's WatchService API.
 * It detects file system events and passes them to the FileSystemModel.
 * @author Van Bao Han Quach
 * @version May 20, 2025
 */
public class FileWatcher {

    /** Java NIO WatchService used for monitoring file system changes. */
    private WatchService myWatchService;

    /** Mapping between WatchKey and directory path. */
    private final Map<WatchKey, Path> myWatchKeys;

    /** The model to report file events to. */
    private final FileSystemModel myModel;

    /** The thread that runs the watch loop. */
    private Thread myWatchThread;

    /** Flag indicating if monitoring is active. */
    private boolean myMonitoring;

    /**
     * Constructs a FileWatcher tied to a model.
     *
     * @param theModel the FileSystemModel to send events to
     */
    public FileWatcher(final FileSystemModel theModel) {
        myModel = theModel;
        myWatchKeys = new HashMap<>();
    }

    /**
     * Starts watching the given directory and its subdirectories.
     *
     * @param theDirectory the root directory to monitor
     * @param theExtensions the set of file extensions to watch (e.g. ".txt")
     */
    public void startWatching(final Path theDirectory, final Set<String> theExtensions) {
        try {
            myWatchService = FileSystems.getDefault().newWatchService();
            registerDirectoryRecursively(theDirectory);

            myMonitoring = true;
            myWatchThread = new Thread(() -> processEvents(theExtensions));
            myWatchThread.start();
        } catch (IOException e) {
            e.printStackTrace(); // Replace with proper error handling
        }
    }

    /**
     * Stops the watching process.
     */
    public void stopWatching() {
        myMonitoring = false;
        try {
            if (myWatchService != null) {
                myWatchService.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a directory and its subdirectories for watch events.
     *
     * @param theRoot the directory to register
     * @throws IOException if an I/O error occurs
     */
    private void registerDirectoryRecursively(final Path theRoot) throws IOException {
        Files.walkFileTree(theRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                WatchKey key = dir.register(myWatchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                myWatchKeys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Handles and processes file system events.
     *
     * @param theExtensions file extensions to filter
     */
    private void processEvents(final Set<String> theExtensions) {
        while (myMonitoring) {
            WatchKey key;
            try {
                key = myWatchService.take(); // waits for events
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            Path dir = myWatchKeys.get(key);
            if (dir == null) continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                Path relativePath = (Path) event.context();
                Path fullPath = dir.resolve(relativePath);
                String fileName = fullPath.getFileName().toString();
                String extension = getExtension(fileName);

                // Check extension match
                if (theExtensions.contains(extension)) {
                    EventType eventType = mapWatchKindToEventType(kind);
                    LocalDateTime now = LocalDateTime.now();

                    FileEvent fileEvent = new FileEvent(
                            fileName,
                            extension,
                            fullPath.toAbsolutePath().toString(),
                            eventType,
                            now
                    );

                    myModel.addEvent(fileEvent);
                }

                // Handle new directories being created
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(fullPath)) {
                            registerDirectoryRecursively(fullPath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                myWatchKeys.remove(key);
                if (myWatchKeys.isEmpty()) break;
            }
        }
    }

    /**
     * Maps Java WatchEvent.Kind to custom EventType enum.
     *
     * @param theKind the watch event kind
     * @return matching EventType
     */
    private EventType mapWatchKindToEventType(WatchEvent.Kind<?> theKind) {
        if (theKind == StandardWatchEventKinds.ENTRY_CREATE) return EventType.CREATED;
        if (theKind == StandardWatchEventKinds.ENTRY_DELETE) return EventType.DELETED;
        if (theKind == StandardWatchEventKinds.ENTRY_MODIFY) return EventType.MODIFIED;
        return EventType.MODIFIED; // fallback
    }

    /**
     * Extracts the file extension from a file name.
     *
     * @param fileName the file name
     * @return the file extension (including the dot), or empty string if none
     */
    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index != -1) ? fileName.substring(index) : "";
    }
}
