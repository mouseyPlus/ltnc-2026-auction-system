package com.auction.controller;

import com.auction.ui.service.MockAuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField identifierField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(
                passwordField.textProperty()
        );

        visiblePasswordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty()
        );
        visiblePasswordField.managedProperty().bind(
                visiblePasswordField.visibleProperty()
        );

        passwordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty().not()
        );
        passwordField.managedProperty().bind(
                passwordField.visibleProperty()
        );
    }

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String identifier = identifierField.getText().trim();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter your account and password.");
            return;
        }

        String username = MockAuthService.login(identifier, password);

        if (username == null) {
            messageLabel.setText("Invalid account or password.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/auction/fxml/home.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/com/auction/css/style.css")
                        .toExternalForm()
        );

        HomeController controller = loader.getController();
        controller.setUsername(username);

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.setTitle("Online Auction - Home");
    }

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/auction/fxml/signup.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/com/auction/css/style.css")
                        .toExternalForm()
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.setTitle("Online Auction - Sign Up");
    }
}
