package mipax.workrewards.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class WorkItem {
    private String id;
    private String title;
    private String description;
    private String category;
    private String dueDate;
    private String dueTime;
    private String hyperlink;
    private boolean completed;
    private String completedAt;

    public WorkItem() {
    }

    public WorkItem(String id, String title, String description, String category, String dueDate, String dueTime, String hyperlink, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.hyperlink = hyperlink;
        setCompleted(completed);
    }

    public WorkItem(String id, String title, String description, String category, String dueDate, String dueTime, String hyperlink, boolean completed, String completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.hyperlink = hyperlink;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            if (this.completedAt == null || this.completedAt.trim().isEmpty()) {
                this.completedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } else {
            this.completedAt = "";
        }
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkItem workItem = (WorkItem) o;
        return Objects.equals(id, workItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toCsvRow() {
        return String.join(";",
                id != null ? id : "",
                title != null ? title : "",
                description != null ? description : "",
                category != null ? category : "",
                dueDate != null ? dueDate : "",
                dueTime != null ? dueTime : "",
                hyperlink != null ? hyperlink : "",
                completed ? "true" : "false",
                completedAt != null ? completedAt : ""
        );
    }

    public static WorkItem fromCsvRow(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] row = csvLine.split(";", -1);
        WorkItem item = new WorkItem();
        item.setId(row.length > 0 ? row[0] : "");
        item.setTitle(row.length > 1 ? row[1] : "");
        item.setDescription(row.length > 2 ? row[2] : "");
        item.setCategory(row.length > 3 ? row[3] : "");
        item.setDueDate(row.length > 4 ? row[4] : "00-00-00");
        item.setDueTime(row.length > 5 ? row[5] : "00:00");
        item.setHyperlink(row.length > 6 ? row[6] : "");
        item.completed = row.length > 7 && "true".equalsIgnoreCase(row[7]);
        item.completedAt = row.length > 8 ? row[8] : "";
        return item;
    }
}
