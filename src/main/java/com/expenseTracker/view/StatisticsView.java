package main.java.com.expenseTracker.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.model.Task;
import main.java.com.expenseTracker.repository.GenericRepository;
import main.java.com.expenseTracker.service.ExpenseService;
import main.java.com.expenseTracker.service.TaskService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsView<T> {

    private final GenericRepository<T> currentRepository;

    public StatisticsView(GenericRepository<T> currentRepository) {
        this.currentRepository = currentRepository;
    }

    public void showStatisticsWindow() {
        Stage stage = new Stage();
        stage.setTitle("Statistics");

        VBox root = new VBox(20);
        root.setPadding(new Insets(15));

        if (currentRepository == null || currentRepository.getAll().isEmpty()) {
            root.getChildren().add(new Label("No data to display."));
        } else {
            if (currentRepository.getType() == Expense.class) {
                ExpenseService expenseService = new ExpenseService(currentRepository);
                root.getChildren().addAll(
                        createTotalExpensesLabel(expenseService),
                        createCategoryPieChart(expenseService),
                        createMonthlyBarChart(expenseService),
                        createYearlyBarChart(expenseService)
                );
                Scene scene = new Scene(root, 600, 780);
                stage.setScene(scene);
            } else if (currentRepository.getType() == Task.class) {
                TaskService taskService = new TaskService(currentRepository);
                root.getChildren().addAll(
                        createCompletedTasksLabel(taskService),
                        createTodayTasksTable(taskService)
                );
                Scene scene = new Scene(root, 600, 400);
                stage.setScene(scene);
            }
        }

        stage.show();
    }

    private Label createTotalExpensesLabel(ExpenseService expenseService) {
        double totalExpenses = expenseService.getTotalExpenses();
        Label label = new Label("Total Expenses: " + totalExpenses);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return label;
    }

    private PieChart createCategoryPieChart(ExpenseService expenseService) {
        Map<String, Double> expensesByCategory = expenseService.getExpensesByCategory();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        expensesByCategory.forEach((category, amount) ->
                pieData.add(new PieChart.Data(category, amount))
        );

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Expenses by category");
        pieChart.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        return pieChart;
    }

    private BarChart<String, Number> createMonthlyBarChart(ExpenseService expenseService) {
        Map<YearMonth, Double> monthlyExpenses = expenseService.getMonthlyExpenses();

        List<Map.Entry<YearMonth, Double>> sortedEntries = new ArrayList<>(monthlyExpenses.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly expenses");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        sortedEntries.forEach(entry ->
                series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()))
        );

        barChart.getData().add(series);

        barChart.setStyle("-fx-font-size: 14px;");
        barChart.setBarGap(5);

        return barChart;
    }

    private BarChart<String, Number> createYearlyBarChart(ExpenseService expenseService) {
        Map<Integer, Double> yearlyExpenses = expenseService.getYearlyExpenses();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Yearly expenses");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        yearlyExpenses.forEach((year, amount) ->
                series.getData().add(new XYChart.Data<>(String.valueOf(year), amount))
        );

        barChart.getData().add(series);
        return barChart;
    }

    private Label createCompletedTasksLabel(TaskService taskService) {
        Map<String, Long> taskCounts = taskService.getTaskCounts();
        long completedTasks = taskCounts.get("completed");
        long totalTasks = taskCounts.get("total");
        Label label = new Label("Finished tasks: " + completedTasks + " / " + totalTasks);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return label;
    }

    private VBox createTodayTasksTable(TaskService taskService) {
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskService.getTodayTasks();

        TableView<Task> taskTable = new TableView<>();
        TableColumn<Task, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Task, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        TableColumn<Task, Boolean> completedColumn = new TableColumn<>("Completed");
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        taskTable.getColumns().addAll(titleColumn, descriptionColumn, priorityColumn, completedColumn);
        taskTable.setItems(FXCollections.observableArrayList(todayTasks));

        Label tableLabel = new Label("Today's Tasks (Incomplete)");
        tableLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(tableLabel, taskTable);

        return vbox;
    }

}

