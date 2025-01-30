package main.java.com.expenseTracker.service;

import main.java.com.expenseTracker.model.Task;
import main.java.com.expenseTracker.repository.GenericRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskService {

    private final GenericRepository<Task> taskRepository;

    public TaskService(GenericRepository<?> repository) {
        if (repository.getType() == Task.class) {
            this.taskRepository = (GenericRepository<Task>) repository;
        } else {
            throw new IllegalArgumentException("Repository type is not compatible with Task.");
        }
    }

    // Pobiera liczbę ukończonych zadań
    public Map<String, Long> getTaskCounts() {
        long completedTasks = taskRepository.getAll().stream()
                .filter(Task::isCompleted)
                .count();
        long totalTasks = taskRepository.getAll().stream()
                .count();
        Map<String, Long> taskCounts = new HashMap<>();
        taskCounts.put("completed", completedTasks);
        taskCounts.put("total", totalTasks);

        return taskCounts;
    }

    // Pobiera liczbę zadań według priorytetu
    public Map<String, Long> getTaskCountByPriority() {
        return taskRepository.getAll().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    // Pobiera liczbę zadań na dzisiejszy dzień
    public List<Task> getTodayTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.getAll().stream()
                .filter(task -> task.getDueDate().equals(today) && !task.isCompleted())
                .collect(Collectors.toList());
    }
}
