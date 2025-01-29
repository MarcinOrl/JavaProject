package main.java.com.expenseTracker.service;

import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.repository.GenericRepository;

import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseService {

    private final GenericRepository<Expense> expenseRepository;

    public ExpenseService(GenericRepository<?> repository) {
        // Sprawdzenie typu repozytorium i rzutowanie na GenericRepository<Expense>
        if (repository.getType() == Expense.class) {
            this.expenseRepository = (GenericRepository<Expense>) repository;
        } else {
            throw new IllegalArgumentException("Repository type is not compatible with Expense.");
        }
    }

    public Map<YearMonth, Double> getMonthlyExpenses() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> YearMonth.from(expense.getDate()),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Inne specyficzne metody
    public Map<String, Double> getExpensesByCategory() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    // Przykład metody do pobierania rocznych wydatków
    public Map<Integer, Double> getYearlyExpenses() {
        return expenseRepository.getAll().stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getDate().getYear(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }
}


