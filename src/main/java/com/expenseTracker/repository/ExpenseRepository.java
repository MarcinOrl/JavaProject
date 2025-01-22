package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseRepository extends GenericRepository<Expense> {

    // Suma wydatków na kategorie
    public Map<String, Double> getExpensesByCategory() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Suma miesięcznych wydatków
    public Map<YearMonth, Double> getMonthlyExpenses() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getDate()),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Suma rocznych wydatków
    public Map<Integer, Double> getYearlyExpenses() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getDate().getYear(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Sortowanie wydatków
    public List<Expense> sortExpenses(Comparator<Expense> comparator) {
        return getAll().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public void exportReport(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Category,Date,Total Amount\n");

            Map<String, Map<LocalDate, Double>> groupedData = getAll().stream()
                    .collect(Collectors.groupingBy(
                            Expense::getCategory,
                            Collectors.groupingBy(
                                    Expense::getDate,
                                    Collectors.summingDouble(Expense::getAmount)
                            )
                    ));

            for (Map.Entry<String, Map<LocalDate, Double>> categoryEntry : groupedData.entrySet()) {
                String category = categoryEntry.getKey();
                for (Map.Entry<LocalDate, Double> dateEntry : categoryEntry.getValue().entrySet()) {
                    writer.write(String.format(Locale.US, "%s,%s,%.2f\n",
                            category, dateEntry.getKey(), dateEntry.getValue()));
                }
            }
            System.out.println("Report exported successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
        }
    }

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
