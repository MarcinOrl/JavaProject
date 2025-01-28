package main.java.com.expenseTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.model.Task;
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
    private TableView<Expense> expenseTable;
    private TableView<Task> taskTable;
    private ComboBox<GenericRepository<?>> expenseRepositoryComboBox;
    private ComboBox<GenericRepository<?>> taskRepositoryComboBox;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Tab expenseTab;
    private Tab taskTab;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Layout glowny
        TabPane tabPane = new TabPane();
        expenseTab = createExpenseTab();
        expenseTab.setClosable(false);
        taskTab = createTaskTab();
        taskTab.setClosable(false);
        tabPane.getTabs().addAll(expenseTab, taskTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == expenseTab) {
                if (currentRepository != getRepositoryForTab(Expense.class)) {
                    currentRepository = getRepositoryForTab(Expense.class);
                }
            } else if (newTab == taskTab) {
                if (currentRepository != getRepositoryForTab(Task.class)) {
                    currentRepository = getRepositoryForTab(Task.class);
                }
            }
        });

        loadRepositories();

        Scene scene = new Scene(tabPane, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Expense and Task Tracker");

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

    // Zakladka Expense
    private Tab createExpenseTab() {
        // Layout
        VBox expenseLayout = new VBox(10);
        expenseLayout.setStyle("-fx-padding: 20;");

        // Wybor repozytorium
        expenseRepositoryComboBox = new ComboBox<>();
        expenseRepositoryComboBox.setPromptText("Choose repository");
        expenseRepositoryComboBox.setOnAction(event -> {
            currentRepository = expenseRepositoryComboBox.getSelectionModel().getSelectedItem();
            updateTable();
        });

        // Tworzenie nowych repozytoriow
        TextField repositoryNameField = new TextField();
        repositoryNameField.setPromptText("Enter repository name");

        Button createRepositoryButton = new Button("Create repository");
        createRepositoryButton.setOnAction(event -> {
            String repositoryName = repositoryNameField.getText();
            createRepository(repositoryName, Expense.class);
            repositoryNameField.clear();
        });

        // Tabela wydatkow
        expenseTable = new TableView<>();
        TableColumn<Expense, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<Expense, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        expenseTable.getColumns().addAll(nameColumn, amountColumn, categoryColumn, dateColumn);

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
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Amount must be a valid number.");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        HBox repositoryControls = new HBox(10, expenseRepositoryComboBox, repositoryNameField, createRepositoryButton);
        VBox form = new VBox(10, nameField, amountField, categoryComboBox, datePicker, addButton);
        HBox buttonBox = new HBox(10, createSaveButton(), createDeleteButton());
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Dodanie do layoutu
        expenseLayout.getChildren().addAll(repositoryControls, expenseTable, form, buttonBox);

        return new Tab("Expenses", expenseLayout);
    }

    private Tab createTaskTab() {
        VBox taskLayout = new VBox(10);
        taskLayout.setStyle("-fx-padding: 20;");

        // Wybor repozytorium
        taskRepositoryComboBox = new ComboBox<>();
        taskRepositoryComboBox.setPromptText("Choose repository");
        taskRepositoryComboBox.setOnAction(event -> {
            currentRepository = taskRepositoryComboBox.getSelectionModel().getSelectedItem();
            updateTable();
        });

        // Tworzenie nowych repozytoriow
        TextField repositoryNameField = new TextField();
        repositoryNameField.setPromptText("Enter repository name");

        Button createRepositoryButton = new Button("Create repository");
        createRepositoryButton.setOnAction(event -> {
            String repositoryName = repositoryNameField.getText();
            createRepository(repositoryName, Task.class);
            repositoryNameField.clear();
        });

        // Tabela zadan
        taskTable = new TableView<>();
        TableColumn<Task, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Task, LocalDate> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        TableColumn<Task, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        TableColumn<Task, Boolean> completedColumn = new TableColumn<>("Completed");
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        taskTable.getColumns().addAll(titleColumn, descriptionColumn, dueDateColumn, priorityColumn, completedColumn);

        // Formularz
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        DatePicker dueDatePicker = new DatePicker(LocalDate.now());
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(getAllowedCategories(Task.class));
        priorityComboBox.setPromptText("Priority");
        CheckBox completedCheckBox = new CheckBox("Completed");
        completedCheckBox.setSelected(false);

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            try {
                String title = titleField.getText();
                String description = descriptionField.getText();
                LocalDate dueDate = dueDatePicker.getValue();
                String priority = priorityComboBox.getValue();
                boolean completed = completedCheckBox.isSelected();

                Task task = new Task(title, description, dueDate, priority, completed);
                Validator.validate(task);
                ((GenericRepository<Task>) currentRepository).add(task);
                updateTable();

                titleField.clear();
                descriptionField.clear();
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        HBox repositoryControls = new HBox(10, taskRepositoryComboBox, repositoryNameField, createRepositoryButton);
        VBox form = new VBox(10, titleField, descriptionField, dueDatePicker, priorityComboBox, completedCheckBox, addButton);
        HBox buttonBox = new HBox(10, createSaveButton(), createDeleteButton());
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        taskLayout.getChildren().addAll(repositoryControls, taskTable, form, buttonBox);

        return new Tab("Tasks", taskLayout);
    }

    private <T> void createRepository(String repositoryName, Class<T> repositoryType) {
        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Repository name cannot be empty.");
            return;
        }
        String typeFolder = "./repositories/" + repositoryType.getSimpleName();
        File directory = new File(typeFolder);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = typeFolder + "/" + repositoryName.trim() + ".json";
        try {
            GenericRepository<T> newRepository = new GenericRepository<>(filePath, repositoryType);
            repositories.add(newRepository);
            if (repositoryType.equals(Expense.class)) {
                expenseRepositoryComboBox.getItems().add(newRepository);
                expenseRepositoryComboBox.getSelectionModel().select(newRepository);
            } else if (repositoryType.equals(Task.class)) {
                taskRepositoryComboBox.getItems().add(newRepository);
                taskRepositoryComboBox.getSelectionModel().select(newRepository);
            }
            currentRepository = newRepository;

            showAlert(Alert.AlertType.INFORMATION, "Success", "Repository '" + repositoryName + "' created successfully.");
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create repository: " + ex.getMessage());
        }
    }

    private void updateTable() {
        if (currentRepository != null) {
            if (expenseTab.isSelected()) {
                expenseTable.getItems().clear();
                expenseTable.getItems().addAll((List<Expense>) currentRepository.getAll());
            } else if (taskTab.isSelected()) {
                taskTable.getItems().clear();
                taskTable.getItems().addAll((List<Task>) currentRepository.getAll());
            }
        }
    }

    private void loadRepositories() {
        File rootDirectory = new File("./repositories");
        File[] typeDirectories = rootDirectory.listFiles(File::isDirectory);

        if (typeDirectories != null) {
            for (File typeDir : typeDirectories) {
                String typeName = typeDir.getName();
                File[] jsonFiles = typeDir.listFiles((dir, name) -> name.endsWith(".json"));

                if (jsonFiles != null) {
                    for (File file : jsonFiles) {
                        try {
                            Class<?> repositoryType = Class.forName("main.java.com.expenseTracker.model." + typeName);
                            GenericRepository<?> repository = new GenericRepository<>(file.getAbsolutePath(), repositoryType);
                            repository.load();
                            repositories.add(repository);
                            if (repositoryType.equals(Expense.class)) {
                                expenseRepositoryComboBox.getItems().add(repository);
                            } else if (repositoryType.equals(Task.class)) {
                                taskRepositoryComboBox.getItems().add(repository);
                            }
                        } catch (Exception ex) {
                            System.err.println("Failed to load repository: " + file.getName() + " Error: " + ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private GenericRepository<?> getRepositoryForTab(Class<?> cl) {
        return repositories.stream()
                .filter(repo -> repo.getType().equals(cl))
                .findFirst()
                .orElse(null);
    }

    private Button createSaveButton() {
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            try {
                if (currentRepository == null) {
                    showAlert(Alert.AlertType.WARNING, "No Repository", "No repository selected. Please select or create a repository first.");
                }
                if (!currentRepository.isDataChanged()) {
                    return;
                }
                currentRepository.save();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save data: " + e.getMessage());
            }
        });
        return saveButton;
    }

    private Button createDeleteButton() {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(event -> {
            Object selectedItem = null; // Ogólny typ dla wybranego elementu

            if (expenseTab.isSelected()) {
                selectedItem = expenseTable.getSelectionModel().getSelectedItem();
            } else if (taskTab.isSelected()) {
                selectedItem = taskTable.getSelectionModel().getSelectedItem();
            }

            if (selectedItem != null && currentRepository != null) {
                try {
                    // Rzutowanie do właściwego typu w repozytorium
                    @SuppressWarnings("unchecked")
                    GenericRepository<Object> repo = (GenericRepository<Object>) currentRepository;
                    repo.delete(selectedItem);

                    if (expenseTab.isSelected()) {
                        expenseTable.getItems().remove(selectedItem);
                    } else if (taskTab.isSelected()) {
                        taskTable.getItems().remove(selectedItem);
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Item deleted successfully.");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete item: " + ex.getMessage());
                }
            } else {
                String message = (selectedItem == null)
                        ? "Please select an item to delete."
                        : "No repository selected.";
                showAlert(Alert.AlertType.WARNING, "No Selection", message);
            }
        });
        return deleteButton;
    }

    private String[] getAllowedCategories(Class<?> cl) {
        try {
            for (Field field : cl.getDeclaredFields()) {
                if (field.isAnnotationPresent(ValidCategory.class)) {
                    return field.getAnnotation(ValidCategory.class).allowedCategories();
                }
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