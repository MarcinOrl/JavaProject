package main.java.com.expenseTracker.repository;

import java.util.List;

public interface Repository<T> {
    void add(T item);
    List<T> getAll();
    void delete(T item);
    void clear();
    void save();
    void update(T item);
    void load();
}
