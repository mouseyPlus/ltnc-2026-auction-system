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

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;

    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(
                passwordField.textProperty()
        );

        visibleConfirmPasswordField.textProperty().bindBidirectional(
                confirmPasswordField.textProperty()
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

        visibleConfirmPasswordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty()
        );
        visibleConfirmPasswordField.managedProperty().bind(
                visibleConfirmPasswordField.visibleProperty()
        );

        confirmPasswordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty().not()
        );
        confirmPasswordField.managedProperty().bind(
                confirmPasswordField.visibleProperty()
        );
    }

    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        String result = MockAuthService.register(
                usernameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                password
        );

        if (result != null) {
            messageLabel.setText(result);
            return;
        }

        openLogin(event);
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        openLogin(event);
    }

    private void openLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/auction/fxml/login.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/com/auction/css/style.css")
                        .toExternalForm()
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.setTitle("Online Auction - Login");
    }
}
