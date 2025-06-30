package com.mycompany.expensetrackerapp;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ExpenseTrackerApp extends Application {

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private TableView<Expense> table = new TableView<>();
    private PieChart pieChart = new PieChart();
    private Label totalAmountLabel = new Label("Total Amount: 0.00 MAD");

    // Input fields for adding/modifying expenses
    private TextField descriptionField = new TextField();
    private TextField amountField = new TextField();
    private ComboBox<String> categoryComboBox = new ComboBox<>();

    // Track the selected expense for modification
    private Expense selectedExpense = null;

    // Track the current month and year
    private String currentMonthYear;

    // Labels for monthly budget, savings, and debt
    private Label monthlyBudgetLabel = new Label("Monthly Budget: 0.00 MAD");
    private Label savingsLabel = new Label("Savings: 0.00 MAD");
    private Label debtLabel = new Label();
    private Label statusIcon = new Label("😊");

    // Input field for monthly budget
    private TextField monthlyBudgetField = new TextField();

    // Progress bar for budget consumption
    private ProgressBar budgetProgressBar = new ProgressBar();
    private Label progressPercentageLabel = new Label("0%");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Show splash screen
        Stage splashStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/splash.fxml"));
            Parent root = loader.load();
            Scene splashScene = new Scene(root);
            splashStage.setScene(splashScene);
            splashStage.initStyle(StageStyle.UNDECORATED); // Remove window decorations
            splashStage.show();
        } catch (IOException ex) {
            Logger.getLogger(ExpenseTrackerApp.class.getName()).log(Level.SEVERE, "Failed to load splash screen", ex);
            Platform.exit(); // Exit if splash screen fails to load
        }

        // Simulate loading delay (e.g., 3 seconds)
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            splashStage.close(); // Close splash screen
            showMainApp(primaryStage); // Open main app
        });
        delay.play();
    }

    private void showMainApp(Stage primaryStage) {
        // Load FontAwesome for icons
        Font.loadFont(getClass().getResourceAsStream("/fontawesome-webfont.ttf"), 12);

        currentMonthYear = getCurrentMonthYear();
        loadExpensesFromFile(); // Load budget and expenses
        updatePieChart();

        primaryStage.setTitle("Expense Tracker");

        // Create layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(15);

        // Add input field for monthly budget
        grid.add(new Label("Monthly Budget:"), 0, 0);
        grid.add(monthlyBudgetField, 1, 0);

        // Add monthly budget, savings, debt, and status icon labels
        HBox statusHBox = new HBox(15);
        statusHBox.setAlignment(Pos.CENTER_LEFT);
        statusHBox.getChildren().addAll(monthlyBudgetLabel, savingsLabel, debtLabel, statusIcon);
        grid.add(statusHBox, 0, 1, 3, 1);

        // Add the date label to the upper right corner
        Label dateLabel = new Label(getCurrentDate());
        grid.add(dateLabel, 2, 0);

        // Add UI components
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        grid.add(new Label("Amount:"), 0, 3);
        grid.add(amountField, 1, 3);

        grid.add(new Label("Category:"), 0, 4);
        categoryComboBox.getItems().addAll("Food", "Transport", "Entertainment", "Utilities", "Other");
        grid.add(categoryComboBox, 1, 4);

        Button addButton = new Button("Add Expense");
        grid.add(addButton, 1, 5);

        // Add a reset button to show all expenses
        Button resetButton = new Button("Show All Expenses");
        grid.add(resetButton, 2, 5);

        // Create a StackPane to overlay the percentage text on the progress bar
        StackPane progressBarContainer = new StackPane();
        progressBarContainer.getStyleClass().add("progress-bar-container");

        // Add the progress bar
        budgetProgressBar.setProgress(0); // Start with 0% consumption
        budgetProgressBar.setPrefWidth(400); // Make progress bar longer
        budgetProgressBar.setPrefHeight(20); // Set height
        budgetProgressBar.getStyleClass().add("budget-progress-bar");

        // Add a label to display the percentage
        progressPercentageLabel.getStyleClass().add("progress-percentage-label");

        // Add the progress bar and label to the StackPane
        progressBarContainer.getChildren().addAll(budgetProgressBar, progressPercentageLabel);

        // Add the StackPane to the grid
        grid.add(new Label("Budget Consumption:"), 0, 7);
        grid.add(progressBarContainer, 1, 7);

        // Move to next field on Enter for descriptionField
        descriptionField.setOnAction(e -> amountField.requestFocus());

        // Move to next field on Enter for amountField
        amountField.setOnAction(e -> categoryComboBox.requestFocus());

        // Move to next field on Enter for categoryComboBox
        categoryComboBox.setOnAction(e -> addButton.requestFocus());

        // Table setup
        TableColumn<Expense, String> descColumn = new TableColumn<>("Description");
        descColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        // Add "Delete" and "Modify" buttons to the table
        TableColumn<Expense, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setMinWidth(150);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Del"); // Delete button with text
            private final Button modifyButton = new Button("Mod"); // Modify button with text

            {
                // Style buttons (optional)
                deleteButton.getStyleClass().add("text-button");
                modifyButton.getStyleClass().add("text-button");

                // Delete button action
                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    expenses.remove(expense);
                    updatePieChart();
                    updateMoneyAndSavings(Double.parseDouble(monthlyBudgetField.getText()));
                    updateBudgetProgressBar(Double.parseDouble(monthlyBudgetField.getText())); // Update progress bar
                });

                // Modify button action
                modifyButton.setOnAction(event -> {
                    selectedExpense = getTableView().getItems().get(getIndex());
                    descriptionField.setText(selectedExpense.getDescription());
                    amountField.setText(String.valueOf(selectedExpense.getAmount()));
                    categoryComboBox.setValue(selectedExpense.getCategory());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(8, modifyButton, deleteButton)); // Arrange buttons horizontally
                }
            }
        });

        table.getColumns().addAll(descColumn, amountColumn, categoryColumn, actionsColumn);
        table.setItems(expenses);

        grid.add(table, 0, 6, 3, 1);

        // Pie Chart setup
        VBox chartBox = new VBox(10);
        chartBox.getChildren().addAll(totalAmountLabel, pieChart);
        grid.add(chartBox, 3, 2, 1, 5);

        // Add expense button action
        addButton.setOnAction(e -> {
            checkMonthChange();

            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText().replace(",", "."));
            String category = categoryComboBox.getValue();

            if (selectedExpense != null) {
                selectedExpense.setDescription(description);
                selectedExpense.setAmount(amount);
                selectedExpense.setCategory(category);
                selectedExpense = null;
            } else {
                Expense expense = new Expense(description, amount, category);
                expenses.add(expense);
            }

            updatePieChart();
            updateMoneyAndSavings(Double.parseDouble(monthlyBudgetField.getText()));
            updateBudgetProgressBar(Double.parseDouble(monthlyBudgetField.getText())); // Update progress bar
            descriptionField.clear();
            amountField.clear();
            categoryComboBox.setValue(null);
        });

        // Reset button action
        resetButton.setOnAction(e -> {
            table.setItems(expenses); // Reset the table to show all expenses
        });

        // Update money and savings when the monthly budget field changes
        monthlyBudgetField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double monthlyBudget = newValue.isEmpty() ? 0.00 : Double.parseDouble(newValue.replace(",", "."));
                monthlyBudgetLabel.setText(String.format("Monthly Budget: %.2f MAD", monthlyBudget));
                updateMoneyAndSavings(monthlyBudget);
                updateBudgetProgressBar(monthlyBudget); // Update progress bar
            } catch (NumberFormatException e) {
                monthlyBudgetLabel.setText("Monthly Budget: Invalid Input");
                savingsLabel.setText("Savings: Invalid Input");
                debtLabel.setVisible(false);
                statusIcon.setText("⚠️");
                statusIcon.getStyleClass().setAll("status-warning");
            }
        });

        // Set up scene
        Scene scene = new Scene(grid, 1000, 600);

        // Load the CSS file
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Set the scene and show the stage
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        saveExpensesToFile();
    }

    private void updatePieChart() {
        pieChart.getData().clear();
        Map<String, Double> categoryAmountMap = new HashMap<>();
        double totalAmount = 0.0;

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getAmount();
            categoryAmountMap.put(category, categoryAmountMap.getOrDefault(category, 0.0) + amount);
            totalAmount += amount;
        }

        totalAmountLabel.setText(String.format("Total Amount: %.2f MAD", totalAmount));

        for (Map.Entry<String, Double> entry : categoryAmountMap.entrySet()) {
            String category = entry.getKey();
            double categoryAmount = entry.getValue();
            double percentage = (categoryAmount / totalAmount) * 100;
            String percentageLabel = String.format("%s (%.2f%%)", category, percentage);
            PieChart.Data data = new PieChart.Data(percentageLabel, categoryAmount);
            pieChart.getData().add(data);

            // Add click event to pie chart slices
            data.getNode().setOnMouseClicked(event -> {
                filterExpensesByCategory(category); // Filter expenses by category
            });
        }
    }

    private void filterExpensesByCategory(String category) {
        ObservableList<Expense> filteredExpenses = FXCollections.observableArrayList();
        for (Expense expense : expenses) {
            if (expense.getCategory().equals(category)) {
                filteredExpenses.add(expense);
            }
        }
        table.setItems(filteredExpenses); // Update table with filtered expenses
    }

    private void updateMoneyAndSavings(double monthlyBudget) {
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double savings = monthlyBudget - totalExpenses;

        if (savings < 0) {
            savings = 0;
        }

        monthlyBudgetLabel.setText(String.format("Monthly Budget: %.2f MAD", monthlyBudget));
        savingsLabel.setText(String.format("Savings: %.2f MAD", savings));

        double debt = 0;
        if (monthlyBudget < totalExpenses) {
            debt = totalExpenses - monthlyBudget;
        }

        if (debt > 0) {
            debtLabel.setText(String.format("Debt: %.2f MAD", debt));
            debtLabel.setVisible(true);
            statusIcon.setText("😢");
            statusIcon.getStyleClass().setAll("status-danger");
        } else {
            debtLabel.setVisible(false);
            statusIcon.setText("😊");
            statusIcon.getStyleClass().setAll("status-positive");
        }
    }

    private void updateBudgetProgressBar(double monthlyBudget) {
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double consumptionPercentage = totalExpenses / monthlyBudget;

        // Ensure the progress bar doesn't exceed 100%
        if (consumptionPercentage > 1.0) {
            consumptionPercentage = 1.0;
        }

        // Update the progress bar
        budgetProgressBar.setProgress(consumptionPercentage);

        // Update the percentage label
        progressPercentageLabel.setText(String.format("%.0f%%", consumptionPercentage * 100));
    }

    private void saveExpensesToFile() {
        String fileName = "expenses_" + getCurrentMonthYear() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Save the monthly budget
            String monthlyBudget = monthlyBudgetLabel.getText().replace("Monthly Budget: ", "").replace(" MAD", "");
            writer.write("MonthlyBudget:" + monthlyBudget);
            writer.newLine();

            // Save savings and debt
            writer.write("Savings:" + savingsLabel.getText().replace("Savings: ", "").replace(" MAD", ""));
            writer.newLine();
            writer.write("Debt:" + (debtLabel.isVisible() ? debtLabel.getText().replace("Debt: ", "").replace(" MAD", "") : "0.00"));
            writer.newLine();

            // Save expenses
            for (Expense expense : expenses) {
                writer.write(expense.getDescription() + "," + expense.getAmount() + "," + expense.getCategory());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadExpensesFromFile() {
        String fileName = "expenses_" + getCurrentMonthYear() + ".txt";
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            double totalExpenses = 0.0; // Track total expenses

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MonthlyBudget:")) {
                    String budgetValue = line.replace("MonthlyBudget:", "").replace(",", ".");
                    if (budgetValue.isEmpty()) {
                        System.err.println("Warning: MonthlyBudget value is empty. Using default value 0.00.");
                        budgetValue = "0.00";
                    }
                    double monthlyBudget = Double.parseDouble(budgetValue);
                    monthlyBudgetLabel.setText(String.format("Monthly Budget: %.2f MAD", monthlyBudget));
                    monthlyBudgetField.setText(String.valueOf(monthlyBudget));
                } else if (line.startsWith("Savings:")) {
                    String savingsValue = line.replace("Savings:", "").replace(",", ".");
                    if (savingsValue.isEmpty()) {
                        System.err.println("Warning: Savings value is empty. Using default value 0.00.");
                        savingsValue = "0.00";
                    }
                    double savings = Double.parseDouble(savingsValue);
                    savingsLabel.setText(String.format("Savings: %.2f MAD", savings));
                } else if (line.startsWith("Debt:")) {
                    String debtValue = line.replace("Debt:", "").replace(",", ".");
                    if (debtValue.isEmpty()) {
                        System.err.println("Warning: Debt value is empty. Using default value 0.00.");
                        debtValue = "0.00";
                    }
                    double debt = Double.parseDouble(debtValue);
                    if (debt > 0) {
                        debtLabel.setText(String.format("Debt: %.2f MAD", debt));
                        debtLabel.setVisible(true);
                    } else {
                        debtLabel.setVisible(false);
                    }
                } else {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String description = parts[0];
                        String amountValue = parts[1].replace(",", ".");
                        if (amountValue.isEmpty()) {
                            System.err.println("Warning: Amount value is empty. Skipping this expense.");
                            continue;
                        }
                        double amount = Double.parseDouble(amountValue);
                        String category = parts[2];
                        expenses.add(new Expense(description, amount, category));

                        // Add to total expenses
                        totalExpenses += amount;
                    }
                }
            }

            // Update progress bar after loading all data
            double monthlyBudget = Double.parseDouble(monthlyBudgetField.getText());
            double consumptionPercentage = totalExpenses / monthlyBudget;

            // Ensure the progress bar doesn't exceed 100%
            if (consumptionPercentage > 1.0) {
                consumptionPercentage = 1.0;
            }

            // Update the progress bar
            budgetProgressBar.setProgress(consumptionPercentage);

            // Update the percentage label
            progressPercentageLabel.setText(String.format("%.0f%%", consumptionPercentage * 100));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentMonthYear() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM");
        return now.format(formatter);
    }

    private String getCurrentDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return now.format(formatter);
    }

    private void checkMonthChange() {
        String newMonthYear = getCurrentMonthYear();
        if (!newMonthYear.equals(currentMonthYear)) {
            expenses.clear();
            currentMonthYear = newMonthYear;
        }
    }
}