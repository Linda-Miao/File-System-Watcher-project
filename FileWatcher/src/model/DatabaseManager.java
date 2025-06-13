
package model;

/* TCSS 360 File watcher project */

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all interactions with the SQLite database.
 * Implements singleton pattern to ensure only one connection is used.
 * Provides methods to insert, query, and clear file events.
 *
 * Note: Requires SQLite JDBC driver.
 *
 * @author Van Bao Han Quach
 * @author Mutahar Wafayee
 * @version June 12, 2025
 */

public class DatabaseManager {

    /** Path to the SQLite database file. */
    private final String myDBPath = "file_events.db";

    /** Connection to the SQLite database. */
    private Connection myConnection;

    /** Singleton instance. */
    private static DatabaseManager myInstance;

    /** Date and time formatter. */
    private final DateTimeFormatter formatter;

    /**
     * Private constructor (singleton pattern).
     */
    private DatabaseManager() {
        connect();
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Returns the singleton instance of the database manager.
     *
     * @return instance of DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (myInstance == null) {
            myInstance = new DatabaseManager();
        }
        return myInstance;
    }

    /**
     * Establishes a connection to the SQLite database.
     */
    private void connect() {
        try {
            myConnection = DriverManager.getConnection("jdbc:sqlite:" + myDBPath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the table for file events if it doesn't already exist.
     */
    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS file_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_name TEXT,
                    file_extension TEXT,
                    path TEXT,
                    event_type TEXT,
                    timestamp TEXT
                );
                """;
        try (Statement stmt = myConnection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a FileEvent into the database.
     *
     * @param theEvent the FileEvent to save
     */
    public void saveToDatabase(final FileEvent theEvent) {
        connect();
        createTableIfNotExists();

        if (theEvent == null) {
            System.err.println("Error: FileEvent is null");
            return;
        }

        if (myConnection == null) {
            System.err.println("Error: Database connection is null after ensureConnection");
            return;
        }
        String sql = """
            INSERT INTO file_events (file_name, file_extension, path, event_type, timestamp)
            VALUES (?, ?, ?, ?, ?);
            """;
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            pstmt.setString(1, theEvent.getMyFileName());
            pstmt.setString(2, theEvent.getMyFileExtension());
            pstmt.setString(3, theEvent.getMyPath());
            pstmt.setString(4, theEvent.getMyEventType().toString());
            pstmt.setString(5, theEvent.getMyTimeStamp().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQLException in saveToDatabase: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception in saveToDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all events from the database.
     */
    public void clearDatabase() {
        String sql = "DELETE FROM file_events;";
        try (Statement stmt = myConnection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queries for all events in the database.
     *
     * @return list of FileEvents
     */
    public List<FileEvent> queryAllEvents() {
        List<FileEvent> results = new ArrayList<>();
        String sql = "SELECT * FROM file_events;";
        try (Statement stmt = myConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(parseResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Helper method to query by one condition.
     */
    private List<FileEvent> queryByCondition(final String clause, final String value) {
        List<FileEvent> results = new ArrayList<>();
        String sql = "SELECT * FROM file_events WHERE " + clause + ";";
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(parseResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Parses a ResultSet row into a FileEvent object.
     */
    private FileEvent parseResultSet(final ResultSet rs) throws SQLException {
        return new FileEvent(
                rs.getString("file_name"),
                rs.getString("file_extension"),
                rs.getString("path"),
                EventType.valueOf(rs.getString("event_type")),
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }

    /**
     * Returns the current database connection (for testing or external use).
     *
     * @return the database connection
     */
    public Connection getConnection() {
        return myConnection;
    }


    /**
     * This method is to query by extension.
     *
     * @param theExtension
     * @return
     */
    public List<FileEvent> queryByExtension(final String theExtension) {
        List<FileEvent> events = new ArrayList<>();
        if (myConnection == null) {
            System.err.println("Error: Database connection is null");
            return events;
        }
        String sql = theExtension == null || theExtension.isEmpty() ?
                "SELECT * FROM file_events" :
                "SELECT * FROM file_events WHERE file_extension = ?";
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            if (theExtension != null && !theExtension.isEmpty()) {
                pstmt.setString(1, theExtension);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(createFileEventFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * This method is to query by date.
     *
     * @param theStart
     * @param theEnd
     * @return
     */
    public List<FileEvent> queryByDateRange(final LocalDateTime theStart, final LocalDateTime theEnd) {
        List<FileEvent> events = new ArrayList<>();
        if (myConnection == null) {
            System.err.println("Error: Database connection is null");
            return events;
        }
        String sql = "SELECT * FROM file_events WHERE timestamp BETWEEN ? AND ?";
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            pstmt.setString(1, theStart.format(formatter));
            pstmt.setString(2, theEnd.format(formatter));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(createFileEventFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * This method is to query by event type.
     *
     * @param theType
     * @return
     */
    public List<FileEvent> queryByEventType(final EventType theType) {
        List<FileEvent> events = new ArrayList<>();
        if (myConnection == null) {
            System.err.println("Error: Database connection is null");
            return events;
        }
        String sql = "SELECT * FROM file_events WHERE event_type = ?";
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            pstmt.setString(1, theType.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(createFileEventFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * This method is to query by directory.
     *
     * @param theDirectoryPath
     * @return
     */
    public List<FileEvent> queryByDirectory(final String theDirectoryPath) {
        List<FileEvent> events = new ArrayList<>();
        if (myConnection == null) {
            System.err.println("Error: Database connection is null");
            return events;
        }
        String sql = "SELECT * FROM file_events WHERE path LIKE ?";
        try (PreparedStatement pstmt = myConnection.prepareStatement(sql)) {
            pstmt.setString(1, theDirectoryPath + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(createFileEventFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * THis method creates file events from reslults.
     *
     * @param rs
     * @throws SQLException
     */
    private FileEvent createFileEventFromResultSet(final ResultSet rs) throws SQLException {
        String fileName = rs.getString("file_name");
        String extension = rs.getString("file_extension");
        String path = rs.getString("path");
        EventType eventType = EventType.valueOf(rs.getString("event_type"));
        String timestam = rs.getString("timestamp");
        LocalDateTime timestamp = LocalDateTime.parse(timestam);
        return new FileEvent(fileName, extension, path, eventType, timestamp);
    }

}