package main.java.com.expenseTracker;

import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.repository.GenericRepository;
import main.java.com.expenseTracker.service.Validator;
import main.java.com.expenseTracker.util.ReflectionUtils;

import java.time.LocalDate;
import java.util.Comparator;

public class MainTesting {
    public static void main(String[] args) {
//        ExpenseRepository expenseRepository = new ExpenseRepository("repo5.json");
        GenericRepository<Expense> expenseRepository = new GenericRepository<>("repo5.json", Expense.class);
        populateRepository(expenseRepository);

        testSaveAndLoad(expenseRepository);
//        testGenerics();
//        testValidation();
//        testStatistics(expenseRepository);
//        testReflectionUtils();
    }

    private static void populateRepository(GenericRepository<Expense> expenseRepository) {
        expenseRepository.add(new Expense("Groceries", 50.00, "Food", LocalDate.of(2025, 1, 10)));
        expenseRepository.add(new Expense("Phone", 1200.00, "Electronics", LocalDate.of(2025, 1, 1)));
        expenseRepository.add(new Expense("Laptop", 2000.00, "Electronics", LocalDate.of(2025, 2, 5)));
        expenseRepository.add(new Expense("Movie", 15.00, "Entertainment", LocalDate.of(2025, 2, 20)));
    }

    private static void testSaveAndLoad(GenericRepository<Expense> expenseRepository) {
        System.out.println("\n=== TEST SAVE AND LOAD ===");
        expenseRepository.save();
        expenseRepository.clear();
        expenseRepository.load();
        expenseRepository.getAll().forEach(System.out::println);
    }

    private static void testGenerics() {
        System.out.println("\n=== TEST GENERICS ===");
        GenericRepository<String> stringRepository = new GenericRepository<>("string.json", String.class);
        stringRepository.add("Hello");
        stringRepository.add("World");
        stringRepository.getAll().forEach(System.out::println);
    }

    private static void testValidation() {
        System.out.println("\n=== TEST VALIDATION ===");
        Expense validExpense = new Expense("Bread", 5.0, "Food & Drinks", LocalDate.now());
        Expense invalidExpense = new Expense("", -10.0, "InvalidCategory", null);

        try {
            System.out.println("Validating validExpense...");
            Validator.validate(validExpense);
            System.out.println("Valid expense is valid");
        } catch (Exception e) {
            System.err.println("Validation error for validExpense: " + e.getMessage());
        }

        try {
            System.out.println("Validating invalidExpense...");
            Validator.validate(invalidExpense);
        } catch (Exception e) {
            System.err.println("Validation error for invalidExpense: " + e.getMessage());
        }
    }

//    private static void testStatistics(ExpenseRepository expenseRepository) {
//        System.out.println("\n=== TEST STATISTICS ===");
//
//        System.out.println("Expenses by category:");
//        expenseRepository.getExpensesByCategory().forEach((category, sum) ->
//                System.out.println(category + ": " + sum));
//
//        System.out.println("\nMonthly expenses:");
//        expenseRepository.getMonthlyExpenses().forEach((month, sum) ->
//                System.out.println(month + ": " + sum));
//
//        System.out.println("\nSorted by amount:");
//        expenseRepository.sortExpenses(Comparator.comparingDouble(Expense::getAmount))
//                .forEach(System.out::println);
//    }

    private static void testReflectionUtils() {
        System.out.println("\n=== TEST REFLECTION UTILS ===");
        Expense expense1 = new Expense("Groceries", 100.0, "Food", LocalDate.of(2025, 1, 10));
        ReflectionUtils.displayObjectDetails(expense1);

        Expense nullExpense = null;
        ReflectionUtils.displayObjectDetails(nullExpense);

        try {
            Expense expense2 = (Expense) ReflectionUtils.createObjectFromClass(
                    "main.java.com.expenseTracker.model.Expense",
                    "Groceries",
                    100.0,
                    "Food",
                    LocalDate.of(2025, 1, 10)
            );
            System.out.println(expense2);
        } catch (Exception e) {
            System.err.println("Error creating object: " + e.getMessage());
        }
    }
}
