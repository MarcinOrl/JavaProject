package main.java.com.expenseTracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.repository.ExpenseRepository;
import main.java.com.expenseTracker.service.Validator;

import java.io.File;
import java.time.LocalDate;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ExpenseRepository repository = new ExpenseRepository();

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
                amount = Math.round(amount * 100) / 100.0;
                String category = categoryField.getText();
                LocalDate date = datePicker.getValue();

                Expense expense = new Expense(name, amount, category, date);
                Validator.validate(expense);

                table.getItems().add(expense);
                repository.add(expense);
                isDataChanged = true;

                nameField.clear();
                amountField.clear();
                categoryField.clear();
                datePicker.setValue(LocalDate.now());

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Amount must be a valid number.");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                if (loadedFilePath != null) {
                    repository.saveExpenses(loadedFilePath);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "No file", "No file loaded. Please load a file before saving.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + ex.getMessage());
            }
        });

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle("Select a file");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                File file = fileChooser.showOpenDialog(primaryStage);

                if (file != null) {
                    loadedFilePath = file.getAbsolutePath();
                    repository.loadExpenses(loadedFilePath);
                    table.getItems().clear();
                    table.getItems().addAll(repository.getAll());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Expenses loaded successfully.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load expenses: " + ex.getMessage());
            }
        });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selectedExpense = table.getSelectionModel().getSelectedItem();
            if (selectedExpense != null) {
                repository.delete(selectedExpense);
                table.getItems().remove(selectedExpense);
                isDataChanged = true;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Expense deleted successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No expense selected.");
            }
        });

        // Event zamykania aplikacji
        primaryStage.setOnCloseRequest(event -> {
            if (isDataChanged) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Exit");
                alert.setHeaderText("Save Changes");
                alert.setContentText("Do you want to save changes before exiting?");

                ButtonType saveButtonExit = new ButtonType("Yes, Save");
                ButtonType exitButtonExit = new ButtonType("No, Exit Without Saving", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType cancelButtonExit = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(saveButtonExit, exitButtonExit, cancelButtonExit);

                alert.showAndWait().ifPresent(response -> {
                    if (response == saveButtonExit) {
                        try {
                            repository.saveExpenses(loadedFilePath);
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully.");
                            isDataChanged = false;
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save data: " + ex.getMessage());
                            event.consume();
                        }
                    } else if (response == cancelButtonExit) {
                        event.consume();
                    }
                });
            }
        });

        HBox buttonBox = new HBox(10, saveButton, loadButton, deleteButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Dodanie do layoutu
        root.getChildren().addAll(table, nameField, amountField, categoryField, datePicker, addButton, buttonBox);

        // Ustawienia sceny
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Expense Tracker");
        primaryStage.show();
    }

    private String loadedFilePath;
    private boolean isDataChanged = false;

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}