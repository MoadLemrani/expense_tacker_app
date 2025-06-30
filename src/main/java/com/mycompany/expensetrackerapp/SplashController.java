/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.expensetrackerapp;

/**
 *
 * @author MON PC
 */
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class SplashController {
    @FXML
    private Label splashLabel;

    public void initialize() {
        splashLabel.setText("Loading Expense Tracker...");
    }
}
