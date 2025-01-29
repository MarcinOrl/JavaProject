package main.java.com.expenseTracker.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.com.expenseTracker.model.Expense;
import main.java.com.expenseTracker.model.Task;
import main.java.com.expenseTracker.repository.GenericRepository;
import main.java.com.expenseTracker.service.ExpenseService;

import java.time.YearMonth;
import java.util.Map;

public class StatisticsView<T> {

    private final GenericRepository<?> currentRepository;

    public StatisticsView(GenericRepository<?> currentRepository) {
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
                root.getChildren().addAll(createCategoryPieChart(expenseService), createMonthlyBarChart(expenseService));
            } else if (currentRepository.getType() == Task.class) {
                root.getChildren().add(createTaskStatsLabel());
            }
        }

        Scene scene = new Scene(root, 600, 600);
        stage.setScene(scene);
        stage.show();
    }

    private PieChart createCategoryPieChart(ExpenseService expenseService) {
        Map<String, Double> expensesByCategory = expenseService.getExpensesByCategory();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Expenses by category");
        return pieChart;
    }

    private BarChart<String, Number> createMonthlyBarChart(ExpenseService expenseService) {
        Map<YearMonth, Double> monthlyExpenses = expenseService.getMonthlyExpenses();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly expenses");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        for (Map.Entry<YearMonth, Double> entry : monthlyExpenses.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        barChart.getData().add(series);
        return barChart;
    }

    private Label createTaskStatsLabel() {
        return new Label("Task statistics here...");
    }
}
