package com.auction.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }
}

