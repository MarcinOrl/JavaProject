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
import main.java.com.expenseTracker.repository.GenericRepository;
import main.java.com.expenseTracker.service.Validator;
import main.java.com.expenseTracker.util.ValidCategory;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    private List<GenericRepository<?>> repositories = new ArrayList<>();
    private GenericRepository<?> currentRepository;
    private ComboBox<GenericRepository<?>> repositoryComboBox;
    private TableView<Object> table;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Layout glowny
        TabPane tabPane = new TabPane();
        Tab expenseTab = createExpenseTab();
        Tab taskTab = createTaskTab();
        tabPane.getTabs().addAll(expenseTab, taskTab);

        Scene scene = new Scene(tabPane, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Expense and Task Tracker");

        loadRepositories();

        // Event zamykania aplikacji
        primaryStage.setOnCloseRequest(event -> {
            boolean unsavedChanges = repositories.stream().anyMatch(GenericRepository::isDataChanged);

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
                            for (GenericRepository<?> repository : repositories) {
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

        primaryStage.show();
    }

    private Tab createExpenseTab() {
        // Layout
        VBox expenseLayout = new VBox(10);
        expenseLayout.setStyle("-fx-padding: 20;");

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
                GenericRepository<Expense> newRepository = new GenericRepository<>(filePath, Expense.class);
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
        TableColumn<Object, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Object, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Object, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        table.getColumns().addAll(nameColumn, amountColumn, categoryColumn, dateColumn);

        // Formularz
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(getAllowedCategories(Expense.class));
        categoryComboBox.setPromptText("Category");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                amount = Math.round(amount * 100) / 100.0;
                String category = categoryComboBox.getValue();
                LocalDate date = datePicker.getValue();

                Expense expense = new Expense(name, amount, category, date);
                Validator.validate(expense);
                ((GenericRepository<Expense>) currentRepository).add(expense);
                updateTable();

                nameField.clear();
                amountField.clear();
                categoryComboBox.getSelectionModel().clearSelection();
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

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selectedExpense = (Expense) table.getSelectionModel().getSelectedItem();
            if (selectedExpense != null && currentRepository != null) {
                ((GenericRepository<Object>) currentRepository).delete(selectedExpense);
                table.getItems().remove(selectedExpense);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data deleted successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No data selected.");
            }
        });

        HBox repositoryControls = new HBox(10, repositoryComboBox, repositoryNameField, createRepositoryButton);
        VBox form = new VBox(10, nameField, amountField, categoryComboBox, datePicker, addButton);
        HBox buttonBox = new HBox(10, saveButton, deleteButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Dodanie do layoutu
        expenseLayout.getChildren().addAll(repositoryControls, table, form, buttonBox);

        return new Tab("Expenses", expenseLayout);
    }

    private Tab createTaskTab() {
        VBox taskLayout = new VBox(10);
        taskLayout.setStyle("-fx-padding: 20;");
        return new Tab("Tasks", taskLayout);
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

    private String[] getAllowedCategories(Class<?> cl) {
        try {
            Field field = cl.getDeclaredField("category");
            if (field.isAnnotationPresent(ValidCategory.class)) {
                return field.getAnnotation(ValidCategory.class).allowedCategories();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}