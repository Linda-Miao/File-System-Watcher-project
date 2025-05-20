// Mutahar Wafayee
package tests;

import model.FileEvent;
import model.EventType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

    @BeforeEach
    public void setUp() {
        fileEvent = new FileEvent(fileName, fileExtension, filePath, eventType, timeStamp);
    }

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
}
