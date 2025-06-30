package com.mycompany.expensetrackerapp;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author MON PC
 */
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Expense {
    private final SimpleStringProperty description;
    private final SimpleDoubleProperty amount;
    private final SimpleStringProperty category;

    public Expense(String description, double amount, String category) {
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public double getAmount() {
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public void setCategory(String category) {
        this.category.set(category);
    }
}