module ExpenseTracker {
    requires javafx.fxml;
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.xml;

    opens main.java.com.expenseTracker.model to com.fasterxml.jackson.databind;

    exports main.java.com.expenseTracker;
    exports main.java.com.expenseTracker.model;
    exports main.java.com.expenseTracker.repository;
    exports main.java.com.expenseTracker.service;
    exports main.java.com.expenseTracker.util;
}