package mipax.workrewards.service;

import mipax.workrewards.model.WorkItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkServiceTest {

    private static final String TEST_DB = "test_WRdb.csv";
    private WorkService workService;

    @BeforeEach
    void setUp() throws IOException {
        File file = new File(TEST_DB);
        if (file.exists()) {
            file.delete();
        }
        workService = new WorkService(TEST_DB);
    }

    @AfterEach
    void tearDown() {
        File file = new File(TEST_DB);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAddWorkAndIdGeneration() {
        WorkItem item1 = new WorkItem(null, "Test Task 1", "Description 1", "Main", "2026-07-30", "12:00", "", false);
        workService.addWork(item1);

        assertEquals("Main-1", item1.getId());
        assertEquals(1, workService.getAllItems().size());

        WorkItem item2 = new WorkItem(null, "Test Task 2", "Description 2", "Main", "2026-07-30", "13:00", "", false);
        workService.addWork(item2);

        assertEquals("Main-2", item2.getId());
        assertEquals(2, workService.getAllItems().size());
    }

    @Test
    void testDeleteWork() {
        WorkItem item = new WorkItem("Main-1", "Task to delete", "Desc", "Main", "2026-07-30", "12:00", "", false);
        workService.addWork(item);

        assertEquals(1, workService.getAllItems().size());
        workService.deleteWork("Main-1");
        assertEquals(0, workService.getAllItems().size());
    }

    @Test
    void testCompletionProgress() {
        WorkItem item1 = new WorkItem("Main-1", "Task 1", "Desc", "Main", "2026-07-30", "12:00", "", true);
        WorkItem item2 = new WorkItem("Main-2", "Task 2", "Desc", "Main", "2026-07-30", "12:00", "", false);

        workService.addWork(item1);
        workService.addWork(item2);

        assertEquals(0.5, workService.getCompletionProgress(), 0.001);
    }

    @Test
    void testCsvSerialization() {
        WorkItem item = new WorkItem("Daily-1", "Daily Chore", "Clean Desk", "Daily", "2026-08-01", "09:00", "https://example.com", true, "2026-07-29 16:00:00");
        String csvRow = item.toCsvRow();

        WorkItem parsed = WorkItem.fromCsvRow(csvRow);
        assertNotNull(parsed);
        assertEquals("Daily-1", parsed.getId());
        assertEquals("Daily Chore", parsed.getTitle());
        assertEquals("Clean Desk", parsed.getDescription());
        assertEquals("Daily", parsed.getCategory());
        assertEquals("2026-08-01", parsed.getDueDate());
        assertEquals("09:00", parsed.getDueTime());
        assertEquals("https://example.com", parsed.getHyperlink());
        assertTrue(parsed.isCompleted());
        assertEquals("2026-07-29 16:00:00", parsed.getCompletedAt());
    }

    @Test
    void testUndoTaskCompletion() {
        WorkItem item = new WorkItem("Main-1", "Active Task", "Desc", "Main", "2026-07-30", "12:00", "", false);
        workService.addWork(item);

        assertFalse(item.isCompleted());
        assertTrue(item.getCompletedAt() == null || item.getCompletedAt().isEmpty());

        // Complete the task
        item.setCompleted(true);
        workService.updateItem(item);
        assertTrue(item.isCompleted());
        assertNotNull(item.getCompletedAt());
        assertFalse(item.getCompletedAt().isEmpty());

        // Undo task
        item.setCompleted(false);
        workService.updateItem(item);
        assertFalse(item.isCompleted());
        assertTrue(item.getCompletedAt().isEmpty());
    }

    @Test
    void testGetCategories() {
        List<String> categories = workService.getCategories();
        assertTrue(categories.contains("Main"));
        assertTrue(categories.contains("Daily"));
        assertTrue(categories.contains("Weekly"));

        WorkItem item = new WorkItem(null, "Custom", "Desc", "Urgent", "2026-07-30", "12:00", "", false);
        workService.addWork(item);

        assertTrue(workService.getCategories().contains("Urgent"));
    }
}
