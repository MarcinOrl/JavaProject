package main.java.com.expenseTracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.service.Validator;

import java.time.LocalDate;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Layout
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        // Tabela wydatków
        TableView<Expense> table = new TableView<>();
        TableColumn<Expense, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(nameColumn, amountColumn, categoryColumn, dateColumn);

        // Formularz
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                String category = categoryField.getText();
                LocalDate date = datePicker.getValue();

                amount = Math.round(amount * 100) / 100.0;

                Expense expense = new Expense(name, amount, category, date);
                Validator.validate(expense);

                table.getItems().add(expense);

                nameField.clear();
                amountField.clear();
                categoryField.clear();
                datePicker.setValue(LocalDate.now());

            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Amount must be a valid number.");
            } catch (IllegalArgumentException ex) {
                showAlert("Validation Error", ex.getMessage());
            } catch (Exception ex) {
                showAlert("Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        // Dodanie do layoutu
        root.getChildren().addAll(table, nameField, amountField, categoryField, datePicker, addButton);

        // Ustawienia sceny
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Expense Tracker");
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}