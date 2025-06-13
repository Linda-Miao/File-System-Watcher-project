package model;

/* TCSS 360 File watcher project */

/**
 * Enum representing types of file system events that can be detected.
 * These correspond to common operations like creation, modification, deletion, and renaming of files.
 *
 * @author Van Bao Han Quach
 * @author Mutahar Wafayee
 * @version June 13, 2025
 */
public enum EventType {

    /** File was created. */
    ENTRY_CREATE,

    /** File was modified. */
    ENTRY_MODIFY,

    /** File was deleted. */
    ENTRY_DELETE,

    /** File was renamed. */
    ENTRY_RENAME
}
