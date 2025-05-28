// Mutahar Wafayee
package tests;

import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the FileEvent class and EventType enum.
 */
public class TestModel {

    private FileEvent fileEvent;
    private final String fileName = "document";
    private final String fileExtension = ".txt";
    private final String filePath = "/home/user/documents";
    private final EventType eventType = EventType.CREATED;
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
        fileEvent.setMyEventType(EventType.MODIFIED);
        assertEquals(EventType.MODIFIED, fileEvent.getMyEventType());
    }

    @Test
    public void testSetMyTimeStamp() {
        LocalDateTime newTime = LocalDateTime.of(2025, 6, 1, 12, 0);
        fileEvent.setMyTimeStamp(newTime);
        assertEquals(newTime, fileEvent.getMyTimeStamp());
    }

    @Test
    public void testToString() {
        String expected = String.format("FileEvent[file=%s, ext=%s, path=%s, type=%s, time=%s]",
                fileName, fileExtension, filePath, eventType, timeStamp);
        assertEquals(expected, fileEvent.toString());
    }

    @Test
    public void testEventTypeEnumValues() {
        EventType[] values = EventType.values();
        assertEquals(4, values.length);
        assertEquals(EventType.CREATED, values[0]);
        assertEquals(EventType.MODIFIED, values[1]);
        assertEquals(EventType.DELETED, values[2]);
        assertEquals(EventType.RENAMED, values[3]);
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
        List<FileEvent> results = db.queryByEventType(EventType.CREATED);
        assertEquals(1, results.size());
        assertEquals(EventType.CREATED, results.get(0).getMyEventType());
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

}
