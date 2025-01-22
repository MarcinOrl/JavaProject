package main.java.com.expenseTracker.repository;

import java.util.ArrayList;
import java.util.List;

public class GenericRepository<T> implements Repository<T> {
    private final List<T> items = new ArrayList<>();

    @Override
    public void add(T item) {
        items.add(item);
        System.out.println("Added: " + item);
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void delete(T item) {
        if (items.remove(item)) {
            System.out.println("Deleted: " + item);
        } else {
            System.out.println("Item not found: " + item);
        }
    }

    @Override
    public void clear() {
        items.clear();
        System.out.println("All items have been cleared.");
    }
}
