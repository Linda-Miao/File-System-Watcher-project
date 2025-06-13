package model;

/* TCSS 360 File watcher project */

import java.time.LocalDateTime;
import java.util.List;

/**
 * The QueryManager provides methods to query the file event database
 * based on various criteria such as date range, file extension, event type, and directory path.
 * It delegates query execution to the DatabaseManager.
 * Acts as a bridge between the QueryView and the DatabaseManager.
 *
 * @author Van Bao Han Quach
 * @author Mutahar Wafayee
 * @version May 20, 2025
 */
public class QueryManager {

    /** Reference to the DatabaseManager singleton. */
    private final DatabaseManager myDBManager;

    /**
     * Constructs a new QueryManager instance.
     */
    public QueryManager() {
        myDBManager = DatabaseManager.getInstance();
    }



    /**
     * Queries the database for events that occurred within a date range.
     *
     * @param theStart the start datetime (inclusive)
     * @param theEnd the end datetime (inclusive)
     * @return list of matching FileEvents
     */
    public List<FileEvent> queryByDateRange(final LocalDateTime theStart, final LocalDateTime theEnd) {
        return myDBManager.queryByDateRange(theStart, theEnd);
    }

    /**
     * Queries the database for events of a specific file extension.
     *
     * @param theExtension the file extension to filter by (e.g. ".txt")
     * @return list of matching FileEvents
     */
    public List<FileEvent> queryByExtension(final String theExtension) {
        return myDBManager.queryByExtension(theExtension);
    }

    /**
     * Queries the database for events with a specific event type.
     *
     * @param theType the event type (e.g. CREATED, MODIFIED)
     * @return list of matching FileEvents
     */
    public List<FileEvent> queryByEventType(final EventType theType) {
        return myDBManager.queryByEventType(theType);
    }

    /**
     * Queries the database for events that occurred in a specific directory path.
     *
     * @param theDirectoryPath the path to search under
     * @return list of matching FileEvents
     */
    public List<FileEvent> queryByDirectoryPath(final String theDirectoryPath) {
        return myDBManager.queryByDirectory(theDirectoryPath);
    }

    /**
     * This method callse the clearDatabase method from DatabaseManager
     */
    public void clearDatabase() {
        myDBManager.clearDatabase();
    }
}
