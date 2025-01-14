package main.java.com.expenseTracker;

import main.java.com.expenseTracker.model.Expense;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("Expense Tracker App started!");

        Expense expense1 = new Expense("Bread", 4.50, "Food & Drinks", LocalDate.now());

        System.out.println(expense1);

        Expense expense2 = new Expense("Laptop", 4200.00, "Electronics", LocalDate.now());

        System.out.println(expense2);
    }
}