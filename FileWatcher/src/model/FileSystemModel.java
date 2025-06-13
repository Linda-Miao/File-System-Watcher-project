package model;

/* TCSS 360 File watcher project */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * The FileSystemModel stores and manages the list of file events
 * detected by the file watcher. It also supports notifying listeners
 * (such as the view) about changes to the event list.
 *
 * This class acts as the model in the MVC architecture.
 * @author Van Bao Han Quach
 * @version May 20, 2025
 */
public class FileSystemModel {

    /** List of all file events captured. */
    private final List<FileEvent> myEvents;

    /** Set of file extensions to monitor. */
    private final List<String> myFileExtensions;

    /** Database connection (can be used by DatabaseManager). */
    private Connection myConnection;

    /** Used to notify views of changes in model state. */
    private final PropertyChangeSupport mySupport;

    /**
     * Constructs a new FileSystemModel instance.
     */
    public FileSystemModel() {
        myEvents = new ArrayList<>();
        myFileExtensions = new ArrayList<>();
        mySupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a new FileEvent to the list and notifies listeners.
     *
     * @param theEvent the file event to add
     */
    public void addEvent(final FileEvent theEvent) {
        myEvents.add(theEvent);
        mySupport.firePropertyChange("fileEventAdded", null, theEvent);
    }

    /**
     * Clears the list of stored file events.
     */
    public void clearEvents() {
        myEvents.clear();
        mySupport.firePropertyChange("fileEventsCleared", null, null);
    }

    /**
     * Returns a copy of the list of file events.
     *
     * @return a list of file events
     */
    public List<FileEvent> getEvents() {
        return new ArrayList<>(myEvents);
    }

    /**
     * Returns the file extensions being watched.
     *
     * @return list of extensions
     */
    public List<String> getFileExtensions() {
        return new ArrayList<>(myFileExtensions);
    }

    /**
     * Replaces the current list of extensions being monitored.
     *
     * @param theExtensions the new list of extensions
     */
    public void setFileExtensions(final List<String> theExtensions) {
        myFileExtensions.clear();
        myFileExtensions.addAll(theExtensions);
    }

    /**
     * Returns the SQLite connection.
     *
     * @return the database connection
     */
    public Connection getConnection() {
        return myConnection;
    }

    /**
     * Sets the SQLite connection.
     *
     * @param theConnection the connection to set
     */
    public void setConnection(final Connection theConnection) {
        myConnection = theConnection;
    }

    /**
     * Adds a property change listener (e.g., the view).
     *
     * @param theListener the listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        mySupport.addPropertyChangeListener(theListener);
    }

    /**
     * Removes a property change listener.
     *
     * @param theListener the listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener theListener) {
        mySupport.removePropertyChangeListener(theListener);
    }
}
