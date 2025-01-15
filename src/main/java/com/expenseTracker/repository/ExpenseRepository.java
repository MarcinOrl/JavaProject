package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Locale;

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

    public void clear() {
        expenses.clear();
        System.out.println("All expenses have been cleared.");
    }

    public void saveExpenses(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Expense expense : expenses) {
                String formattedAmount = String.format(Locale.US, "%.2f", expense.getAmount());
                writer.write(String.format("%s,%s,%s,%s\n",
                        expense.getName(),
                        formattedAmount,
                        expense.getCategory(),
                        expense.getDate()
                ));
            }
            System.out.println("Expenses saved successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }


    public void loadExpenses(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Expense expense = new Expense(
                            parts[0],
                            Double.parseDouble(parts[1]),
                            parts[2],
                            LocalDate.parse(parts[3])
                    );
                    expenses.add(expense);
                }
            }
            System.out.println("Expenses loaded from file: " + filePath);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        }
    }
}
