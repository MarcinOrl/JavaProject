package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRepository extends GenericRepository<Task> {

    public TaskRepository(String filePath) {
        super(filePath, Task.class);
    }

    // Pobiera zadania na dzisiejszy dzień
    public List<Task> getTasksForToday() {
        LocalDate today = LocalDate.now();
        return getAll().stream()
                .filter(task -> task.getDueDate().equals(today))
                .collect(Collectors.toList());
    }

    // Pobiera zadania o określonym priorytecie
    public List<Task> getTasksByPriority(int priority) {
        return getAll().stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    // Pobiera wszystkie zakończone zadania
    public List<Task> getCompletedTasks() {
        return getAll().stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }
}
