package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Task;

public class TaskRepository extends GenericRepository<Task> {

    public TaskRepository(String filePath) {
        super(filePath, Task.class);
    }
}
