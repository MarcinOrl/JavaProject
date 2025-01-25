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
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    private List<ExpenseRepository> repositories = new ArrayList<>();
    private ExpenseRepository currentRepository;
    private ComboBox<ExpenseRepository> repositoryComboBox;
    private TableView<Expense> table;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Layout
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        // Wybor repozytorium
        repositoryComboBox = new ComboBox<>();
        repositoryComboBox.setPromptText("Choose repository");
        repositoryComboBox.setOnAction(event -> {
            currentRepository = repositoryComboBox.getSelectionModel().getSelectedItem();
            updateTable();
        });

        // Tworzenie nowych repozytoriów
        TextField repositoryNameField = new TextField();
        repositoryNameField.setPromptText("Enter repository name");

        Button createRepositoryButton = new Button("Create repository");
        createRepositoryButton.setOnAction(event -> {
            String repositoryName = repositoryNameField.getText().trim();
            if (repositoryName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Repository name cannot be empty.");
                return;
            }
            try {
                String filePath = "./repositories/" + repositoryName + ".json";
                ExpenseRepository newRepository = new ExpenseRepository(filePath);
                repositories.add(newRepository);
                repositoryComboBox.getItems().add(newRepository);
                repositoryComboBox.getSelectionModel().select(newRepository);
                currentRepository = newRepository;
                repositoryNameField.clear();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Repository '" + repositoryName + "' created successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create repository: " + ex.getMessage());
            }
        });

        // Tabela wydatków
        table = new TableView<>();
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
        datePicker.setValue(LocalDate.now());
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
                currentRepository.add(expense);

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
                if (currentRepository == null) {
                    showAlert(Alert.AlertType.WARNING, "No Repository", "No repository selected. Please select or create a repository first.");
                }
                if (!currentRepository.isDataChanged()) {
                    return;
                }
                currentRepository.save();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save data: " + ex.getMessage());
            }
        });

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                fileChooser.setTitle("Select a file");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                File file = fileChooser.showOpenDialog(primaryStage);

                if (file != null) {
                    loadedFilePath = file.getAbsolutePath();
                    loadRepositoryFromFile(loadedFilePath);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Data loaded successfully.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + ex.getMessage());
            }
        });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selectedExpense = table.getSelectionModel().getSelectedItem();
            if (selectedExpense != null) {
                currentRepository.delete(selectedExpense);
                table.getItems().remove(selectedExpense);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data deleted successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No data selected.");
            }
        });

        // Event zamykania aplikacji
        primaryStage.setOnCloseRequest(event -> {
            boolean unsavedChanges = false;

            for (ExpenseRepository repository : repositories) {
                if (repository.isDataChanged()) {
                    unsavedChanges = true;
                    break;
                }
            }

            if (unsavedChanges) {
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
                            for (ExpenseRepository repository : repositories) {
                                if (repository.isDataChanged()) {
                                    repository.save();
                                }
                            }
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

        HBox repositoryControls = new HBox(10, repositoryComboBox, repositoryNameField, createRepositoryButton);
        HBox buttonBox = new HBox(10, saveButton, loadButton, deleteButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Dodanie do layoutu
        root.getChildren().addAll(repositoryControls, table, nameField, amountField, categoryField, datePicker, addButton, buttonBox);

        loadRepositories();

        // Ustawienia sceny
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Expense Tracker");
        primaryStage.show();
    }

    private String loadedFilePath;

    public void loadRepositoryFromFile(String filePath) {
        ExpenseRepository repository = new ExpenseRepository(filePath);
        repository.load();
        repositories.add(repository);
        repositoryComboBox.getItems().add(repository);
        repositoryComboBox.getSelectionModel().select(repository);
        currentRepository = repository;
        updateTable();
    }

    private void updateTable() {
        table.getItems().clear();
        if (currentRepository != null) {
            table.getItems().addAll(currentRepository.getAll());
        }
    }

    private void loadRepositories() {
        File directory = new File("./repositories");
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles != null) {
            for (File file : jsonFiles) {
                try {
                    ExpenseRepository repository = new ExpenseRepository(file.getAbsolutePath());
                    repository.load();
                    repositories.add(repository);
                    repositoryComboBox.getItems().add(repository);
                } catch (Exception ex) {
                    System.err.println("Failed to load repository: " + file.getName() + " Error: " + ex.getMessage());
                }
            }
        }

        if (!repositories.isEmpty()) {
            repositoryComboBox.getSelectionModel().select(0);
            currentRepository = repositories.getFirst();
            updateTable();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}