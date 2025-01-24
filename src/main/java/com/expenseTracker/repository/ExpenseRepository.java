package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseRepository extends GenericRepository<Expense> {

    public ExpenseRepository(String filePath) {
        super(filePath, Expense.class);
    }

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
}
