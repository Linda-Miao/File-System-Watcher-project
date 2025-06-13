package model;

/* TCSS 360 File watcher project */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a file system event (e.g. file creation, modification, deletion, renaming).
 * Stores information about the file, the type of event, and the timestamp.
 *
 * @author Van Bao Han Quach
 * @author Mutahar Wafayee
 * @version June 13, 2025
 */
public class FileEvent {

    /** Name of the file involved in the event. */
    private String myFileName;

    /** Extension of the file. */
    private String myFileExtension;

    /** Absolute path to the file's location. */
    private String myPath;

    /** Type of file event (e.g. CREATED, MODIFIED). */
    private EventType myEventType;

    /** Timestamp when the event occurred. */
    private LocalDateTime myTimeStamp;

    /**
     * Constructs a FileEvent with all required details.
     *
     * @param theFileName Name of the file.
     * @param theFileExtension File extension (e.g. ".txt").
     * @param thePath Absolute path to the file.
     * @param theEventType Type of event (e.g. CREATED, MODIFIED).
     * @param theTimeStamp The time the event occurred.
     */
    public FileEvent(final String theFileName, final String theFileExtension, final String thePath, final EventType theEventType, final LocalDateTime theTimeStamp) {
        this.myFileName = theFileName;
        this.myFileExtension = theFileExtension;
        this.myPath = thePath;
        this.myEventType = theEventType;
        this.myTimeStamp = theTimeStamp;
    }

    /**
     * Gets the file name.
     *
     * @return The file name.
     */
    public String getMyFileName() {
        return myFileName;
    }

    /**
     * Sets the file name.
     *
     * @param theFileName The file name to set.
     */
    public void setMyFileName(final String theFileName) {
        this.myFileName = theFileName;
    }

    /**
     * Gets the file extension.
     *
     * @return The file extension.
     */
    public String getMyFileExtension() {
        return myFileExtension;
    }

    /**
     * Sets the file extension.
     *
     * @param theFileExtension The file extension to set.
     */
    public void setMyFileExtension(final String theFileExtension) {
        this.myFileExtension = theFileExtension;
    }

    /**
     * Gets the full path to the file.
     *
     * @return The file path.
     */
    public String getMyPath() {
        return myPath;
    }

    /**
     * Sets the full path to the file.
     *
     * @param thePath The file path to set.
     */
    public void setMyPath(final String thePath) {
        this.myPath = thePath;
    }

    /**
     * Gets the type of file event.
     *
     * @return The event type.
     */
    public EventType getMyEventType() {
        return myEventType;
    }

    /**
     * Sets the type of file event.
     *
     * @param theEventType The event type to set.
     */
    public void setMyEventType(final EventType theEventType) {
        this.myEventType = theEventType;
    }

    /**
     * Gets the timestamp of the event.
     *
     * @return The event timestamp.
     */
    public LocalDateTime getMyTimeStamp() {
        return myTimeStamp;
    }

    /**
     * Sets the timestamp of the event.
     *
     * @param theTimeStamp The timestamp to set.
     */
    public void setMyTimeStamp(final LocalDateTime theTimeStamp) {
        this.myTimeStamp = theTimeStamp;
    }

    /**
     * Returns a string representation of the FileEvent.
     *
     * @return A formatted string showing all fields.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("FileEvent[file=%s, ext=%s, path=%s, type=%s, time=%s]",
                myFileName, myFileExtension, myPath, myEventType, myTimeStamp.format(formatter));
    }

}
