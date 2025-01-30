package main.java.com.expenseTracker.repository;

import main.java.com.expenseTracker.model.Expense;

public class ExpenseRepository extends GenericRepository<Expense> {

    public ExpenseRepository(String filePath) {
        super(filePath, Expense.class);
    }
}
