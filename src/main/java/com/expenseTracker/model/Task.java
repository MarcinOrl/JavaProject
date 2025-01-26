package main.java.com.expenseTracker.model;

import main.java.com.expenseTracker.util.MinValue;
import main.java.com.expenseTracker.util.NotNull;

import java.time.LocalDate;

public class Task {
    @NotNull(message = "Title cannot be null or empty")
    private String title;

    @NotNull(message = "Due date cannot be null")
    private LocalDate dueDate;

    @MinValue(value = 1, message = "Priority must be greater than 0")
    private int priority;

    private boolean completed;

    public Task(String title, LocalDate dueDate, int priority, boolean completed) {
        this.title = title;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
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
