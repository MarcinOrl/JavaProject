package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository implements Repository<Expense> {
    private final List<Expense> expenses = new ArrayList<>();

    @Override
    public void add(Expense item) {
        expenses.add(item);
        System.out.println("Added: " + item);
    }

    @Override
    public List<Expense> getAll() {
        return new ArrayList<>(expenses);
    }

    @Override
    public void delete(Expense item) {
        if (expenses.remove(item)) {
            System.out.println("Deleted: " + item);
        } else {
            System.out.println("Item not found: " + item);
        }
    }

    public void saveExpenses(List<Expense> expenses, String filePath) {
        // TODO: Implementacja zapisu do plików CSV/JSON
    }

    public List<Expense> loadExpenses(String filePath) {
        // TODO: Implementacja odczytu z plików CSV/JSON
        return null;
    }
}
