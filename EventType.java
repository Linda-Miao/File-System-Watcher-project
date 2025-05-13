/**
 * Enum representing types of file system events that can be detected.
 * These correspond to common operations like creation, modification, deletion, and renaming of files.
 *
 * @author Van Bao Han Quach
 * @version May 13, 2025
 */
public enum EventType {

    /** File was created. */
    CREATED,

    /** File was modified. */
    MODIFIED,

    /** File was deleted. */
    DELETED,

    /** File was renamed. */
    RENAMED
}
