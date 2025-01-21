package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

import java.io.*;
import java.time.LocalDate;
import java.util.Locale;

public class ExpenseRepository extends GenericRepository<Expense> {
    public void saveExpenses(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Expense expense : getAll()) {
                String formattedAmount = String.format(Locale.US, "%.2f", expense.getAmount());
                writer.write(String.format("%s,%s,%s,%s\n",
                        expense.getName(),
                        formattedAmount,
                        expense.getCategory(),
                        expense.getDate()
                ));
            }
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
                    add(expense);
                }
            }
            System.out.println("Expenses (" + getAll().size() +  ") loaded from file: " + filePath);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        }
    }
}
