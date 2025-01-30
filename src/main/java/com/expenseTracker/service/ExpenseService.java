package main.java.com.expenseTracker.service;

import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.repository.GenericRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseService {

    private final GenericRepository<Expense> expenseRepository;

    public ExpenseService(GenericRepository<?> repository) {
        if (repository.getType() == Expense.class) {
            this.expenseRepository = (GenericRepository<Expense>) repository;
        } else {
            throw new IllegalArgumentException("Repository type is not compatible with Expense.");
        }
    }

    // Suma wydatków na kategorie
    public Map<String, Double> getExpensesByCategory() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Suma miesięcznych wydatków
    public Map<YearMonth, Double> getMonthlyExpenses() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getDate()),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Suma rocznych wydatków
    public Map<Integer, Double> getYearlyExpenses() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getDate().getYear(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Suma wszystkich wydatków
    public double getTotalExpenses() {
        return expenseRepository.getAll().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Sortowanie wydatków
    @Deprecated
    public List<Expense> sortExpenses(Comparator<Expense> comparator) {
        return expenseRepository.getAll().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Deprecated
    public void exportReport(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Category,Date,Total Amount\n");

            Map<String, Map<LocalDate, Double>> groupedData = expenseRepository.getAll().stream()
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


