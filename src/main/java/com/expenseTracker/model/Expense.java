package main.java.com.expenseTracker.model;

import java.time.LocalDate;
import java.util.Locale;
import main.java.com.expenseTracker.util.NotNull;

public class Expense {

    @NotNull(message = "Name cannot be null or empty")
    private String name;

    @NotNull(message = "Amount cannot be null or zero")
    private Double amount;

    @NotNull(message = "Category cannot be null or empty")
    private String category;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    public Expense(String name, double amount, String category, LocalDate date) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override public String toString() {
        return String.format(Locale.US, "Expense{name='%s', amount=%.2f, category='%s', date=%s}",
                name, amount, category, date
        );
    }
}
