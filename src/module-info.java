module ExpenseTracker {
    requires javafx.fxml;
    requires javafx.controls;

    exports main.java.com.expenseTracker;
    exports main.java.com.expenseTracker.model;
    exports main.java.com.expenseTracker.repository;
    exports main.java.com.expenseTracker.service;
    exports main.java.com.expenseTracker.util;
}