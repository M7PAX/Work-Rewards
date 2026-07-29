package mipax.workrewards.service;

import mipax.workrewards.model.WorkItem;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class WorkService {
    private String dbFileName = "WRdb.csv";
    private final List<WorkItem> items = new ArrayList<>();

    public WorkService() {
        load();
    }

    public WorkService(String dbFileName) {
        this.dbFileName = dbFileName;
        load();
    }

    public synchronized void load() {
        items.clear();
        File csv = new File(dbFileName);
        if (!csv.exists()) {
            try {
                csv.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            List<String> lines = Files.readAllLines(csv.toPath());
            for (String line : lines) {
                WorkItem item = WorkItem.fromCsvRow(line);
                if (item != null && item.getId() != null && !item.getId().trim().isEmpty()) {
                    items.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void save() {
        try (FileWriter writer = new FileWriter(dbFileName)) {
            for (WorkItem item : items) {
                writer.write(item.toCsvRow() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<WorkItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public synchronized List<String> getCategories() {
        Set<String> categorySet = new LinkedHashSet<>(List.of("Main", "New", "Daily", "Weekly", "Monthly", "Yearly"));
        for (WorkItem item : items) {
            if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                categorySet.add(item.getCategory().trim());
            }
        }
        return new ArrayList<>(categorySet);
    }

    public synchronized String generateId(String category) {
        String cat = (category != null && !category.trim().isEmpty()) ? category.trim() : "Main";
        long count = items.stream()
                .filter(i -> cat.equalsIgnoreCase(i.getCategory()))
                .count();
        return cat + "-" + (count + 1);
    }

    public synchronized void addWork(WorkItem item) {
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            item.setCategory("Main");
        }
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            item.setId(generateId(item.getCategory()));
        }
        items.add(item);
        save();
    }

    public synchronized void deleteWork(String id) {
        items.removeIf(item -> Objects.equals(item.getId(), id));
        save();
    }

    public synchronized void updateItem(WorkItem updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getId(), updatedItem.getId())) {
                items.set(i, updatedItem);
                break;
            }
        }
        save();
    }

    public synchronized double getCompletionProgress() {
        if (items.isEmpty()) {
            return 0.0;
        }
        long completedCount = items.stream().filter(WorkItem::isCompleted).count();
        return (double) completedCount / items.size();
    }

    public String getDbFileName() {
        return dbFileName;
    }
}
