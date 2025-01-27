package main.java.com.expenseTracker.model;

import main.java.com.expenseTracker.util.MinValue;
import main.java.com.expenseTracker.util.NotNull;
import main.java.com.expenseTracker.util.ValidCategory;

import java.time.LocalDate;

public class Task {
    @NotNull(message = "Title cannot be null or empty")
    private String title;

    private String description;

    @NotNull(message = "Due date cannot be null")
    private LocalDate dueDate;

    @MinValue(value = 1, message = "Priority must be greater than 0")
    @ValidCategory(allowedCategories = {"1", "2", "3", "4", "5"}, message = "Invalid priority.")
    private String priority;

    private boolean completed;

    public Task() {}

    public Task(String title, String description, LocalDate dueDate, String priority, boolean completed) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return String.format("Task{title='%s', dueDate=%s, priority=%d, completed=%s}",
                title, dueDate, priority, completed);
    }
}
