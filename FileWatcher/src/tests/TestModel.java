
package tests;

/* TCSS 360 File watcher project */

import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.mail.MessagingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the FileEvent class and EventType enum.
 *
 * @author Mutahar Wafayee
 * @version June 12, 2025
 */
public class TestModel {

    private FileEvent fileEvent;
    private final String fileName = "document";
    private final String fileExtension = ".txt";
    private final String filePath = "/home/user/documents";
    private final EventType eventType = EventType.ENTRY_CREATE;
    private final LocalDateTime timeStamp = LocalDateTime.of(2025, 5, 13, 10, 30);

    private DatabaseManager db;

    private FileSystemModel fsModel;

    @BeforeEach
    public void setUp() {
        fileEvent = new FileEvent(fileName, fileExtension, filePath, eventType, timeStamp);
        db = DatabaseManager.getInstance();
        db.clearDatabase();

        fsModel = new FileSystemModel();
    }

    // Tests for FileEvent
    @Test
    public void testConstructorAndGetters() {
        assertEquals(fileName, fileEvent.getMyFileName());
        assertEquals(fileExtension, fileEvent.getMyFileExtension());
        assertEquals(filePath, fileEvent.getMyPath());
        assertEquals(eventType, fileEvent.getMyEventType());
        assertEquals(timeStamp, fileEvent.getMyTimeStamp());
    }

    @Test
    public void testSetMyFileName() {
        fileEvent.setMyFileName("newName");
        assertEquals("newName", fileEvent.getMyFileName());
    }

    @Test
    public void testSetMyFileExtension() {
        fileEvent.setMyFileExtension(".java");
        assertEquals(".java", fileEvent.getMyFileExtension());
    }

    @Test
    public void testSetMyPath() {
        fileEvent.setMyPath("/new/path");
        assertEquals("/new/path", fileEvent.getMyPath());
    }

    @Test
    public void testSetMyEventType() {
        fileEvent.setMyEventType(EventType.ENTRY_MODIFY);
        assertEquals(EventType.ENTRY_MODIFY, fileEvent.getMyEventType());
    }

    @Test
    public void testSetMyTimeStamp() {
        LocalDateTime newTime = LocalDateTime.of(2025, 6, 1, 12, 0);
        fileEvent.setMyTimeStamp(newTime);
        assertEquals(newTime, fileEvent.getMyTimeStamp());
    }


    // Tests for EventType class
    @Test
    public void testEventTypeEnumValues() {
        EventType[] values = EventType.values();
        assertEquals(4, values.length);
        assertEquals(EventType.ENTRY_CREATE, values[0]);
        assertEquals(EventType.ENTRY_MODIFY, values[1]);
        assertEquals(EventType.ENTRY_DELETE, values[2]);
        assertEquals(EventType.ENTRY_RENAME, values[3]);
    }


    // DatabaseManager Tests

    @Test
    public void testSaveAndQueryAllEvents() {
        db.saveToDatabase(fileEvent);
        List<FileEvent> results = db.queryAllEvents();
        assertEquals(1, results.size());
        assertEquals(fileEvent.getMyFileName(), results.get(0).getMyFileName());
    }

    @Test
    public void testClearDatabase() {
        db.saveToDatabase(fileEvent);
        db.clearDatabase();
        List<FileEvent> results = db.queryAllEvents();
        assertTrue(results.isEmpty());
    }

    @Test
    public void testQueryByExtension() {
        db.saveToDatabase(fileEvent);
        List<FileEvent> results = db.queryByExtension(".txt");
        assertEquals(1, results.size());
        assertEquals(".txt", results.get(0).getMyFileExtension());
    }

    @Test
    public void testQueryByEventType() {
        db.saveToDatabase(fileEvent);
        List<FileEvent> results = db.queryByEventType(EventType.ENTRY_CREATE);
        assertEquals(1, results.size());
        assertEquals(EventType.ENTRY_CREATE, results.get(0).getMyEventType());
    }

    @Test
    public void testQueryByDirectory() {
        db.saveToDatabase(fileEvent);
        List<FileEvent> results = db.queryByDirectory("/home/user");
        assertEquals(1, results.size());
        assertTrue(results.get(0).getMyPath().startsWith("/home/user"));
    }

    @Test
    public void testQueryByDateRange() {
        db.saveToDatabase(fileEvent);
        LocalDateTime start = LocalDateTime.of(2025, 5, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 5, 14, 0, 0);
        List<FileEvent> results = db.queryByDateRange(start, end);
        assertEquals(1, results.size());
        assertEquals(fileEvent.getMyTimeStamp(), results.get(0).getMyTimeStamp());
    }


    // Tests for FileSystemModel

    @Test
    public void testAddAndGetEvents() {
        fsModel.addEvent(fileEvent);
        List<FileEvent> events = fsModel.getEvents();
        assertEquals(1, events.size());
        assertEquals(fileEvent, events.get(0));
    }

    @Test
    public void testClearEvents() {
        fsModel.addEvent(fileEvent);
        fsModel.clearEvents();
        assertTrue(fsModel.getEvents().isEmpty());
    }

    @Test
    public void testSetAndGetFileExtensions() {
        List<String> extensions = List.of(".txt", ".java");
        fsModel.setFileExtensions(extensions);
        List<String> result = fsModel.getFileExtensions();
        assertEquals(2, result.size());
        assertTrue(result.contains(".txt"));
        assertTrue(result.contains(".java"));
    }

    @Test
    public void testSetAndGetConnection() {
        Connection dummyConnection = db.getConnection(); // Just get a real connection
        fsModel.setConnection(dummyConnection);
        assertEquals(dummyConnection, fsModel.getConnection());
    }

    @Test
    public void testPropertyChangeListeners() {
        List<PropertyChangeEvent> changes = new ArrayList<>();
        PropertyChangeListener listener = changes::add;
        fsModel.addPropertyChangeListener(listener);

        fsModel.addEvent(fileEvent);
        assertEquals(1, changes.size());
        assertEquals("fileEventAdded", changes.get(0).getPropertyName());
        assertEquals(fileEvent, changes.get(0).getNewValue());

        fsModel.clearEvents();
        assertEquals(2, changes.size());
        assertEquals("fileEventsCleared", changes.get(1).getPropertyName());

        fsModel.removePropertyChangeListener(listener);
    }

    //Tests for FileWatcher class
    @Test
    public void testFileWatcherConstructor() {
        FileSystemModel model = new FileSystemModel();
        FileWatcher watcher = new FileWatcher(model);
        assertNotNull(watcher);
    }


    @Test
    public void testGetExtension_ValidAndInvalid() throws Exception {
        FileSystemModel model = new FileSystemModel();
        FileWatcher watcher = new FileWatcher(model);

        // Use reflection to access private method
        var method = FileWatcher.class.getDeclaredMethod("getExtension", String.class);
        method.setAccessible(true);

        assertEquals(".txt", method.invoke(watcher, "notes.txt"));
        assertEquals(".java", method.invoke(watcher, "TestFile.java"));
        assertEquals("", method.invoke(watcher, "README"));
    }

    @Test
    public void testMapWatchKindToEventType() throws Exception {
        FileSystemModel model = new FileSystemModel();
        FileWatcher watcher = new FileWatcher(model);

        var method = FileWatcher.class.getDeclaredMethod("mapWatchKindToEventType", WatchEvent.Kind.class);
        method.setAccessible(true);

        assertEquals(EventType.ENTRY_CREATE, method.invoke(watcher, StandardWatchEventKinds.ENTRY_CREATE));
        assertEquals(EventType.ENTRY_MODIFY, method.invoke(watcher, StandardWatchEventKinds.ENTRY_MODIFY));
        assertEquals(EventType.ENTRY_DELETE, method.invoke(watcher, StandardWatchEventKinds.ENTRY_DELETE));
    }


    @Test
    public void testStartAndStopWatching(@TempDir Path tempDir) throws IOException, InterruptedException {
        FileSystemModel model = new FileSystemModel();
        FileWatcher watcher = new FileWatcher(model);

        Set<String> extensions = Set.of(".txt");
        watcher.startWatching(tempDir, extensions);

        // Wait briefly to simulate running
        Thread.sleep(500);

        watcher.stopWatching();

        // Wait for the thread to clean up
        Thread.sleep(200);

        // If no exception, thread ran and was stopped successfully
        assertTrue(true);
    }

    // Tests for QueryManager class
    @Test
    public void testQueryByExtension_EmptyResult() {
        QueryManager queryManager = new QueryManager();
        queryManager.clearDatabase();
        List<FileEvent> results = queryManager.queryByExtension(".log");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testQueryByEventType_EmptyResult() {
        QueryManager queryManager = new QueryManager();
        queryManager.clearDatabase();
        List<FileEvent> results = queryManager.queryByEventType(EventType.ENTRY_DELETE);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testQueryByDateRange_EmptyResult() {
        QueryManager queryManager = new QueryManager();
        queryManager.clearDatabase();
        LocalDateTime now = LocalDateTime.now();
        List<FileEvent> results = queryManager.queryByDateRange(now.minusDays(1), now);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testQueryByDirectoryPath_EmptyResult() {
        QueryManager queryManager = new QueryManager();
        queryManager.clearDatabase();
        List<FileEvent> results = queryManager.queryByDirectoryPath("/some/fake/path");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testClearDatabase_NoErrors() {
        QueryManager queryManager = new QueryManager();
        assertDoesNotThrow(queryManager::clearDatabase);
    }



    // Tests for Email Service class
    private EmailService emailService;

    @BeforeEach
    public void setUp2() {
        emailService = new EmailService("sender@example.com", "fake-password");
    }

    @Test
    public void testConstructor_NotNull() {
        assertNotNull(emailService);
    }

    @Test
    public void testSendEmailWithAttachment_MissingFile_ThrowsException() {
        File fakeFile = new File("nonexistent.csv");

        Exception exception = assertThrows(Exception.class, () -> {
            emailService.sendEmailWithAttachment(
                    "recipient@example.com",
                    "Test Subject",
                    "Test Body",
                    fakeFile
            );
        });

        // Assert that either IOException or MessagingException is thrown
        boolean isExpected = exception instanceof IOException
                || exception instanceof MessagingException
                || (exception.getCause() != null && exception.getCause() instanceof IOException);

        assertTrue(isExpected, "Expected IOException or MessagingException due to missing file");
    }


}
