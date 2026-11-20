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

    @FXML private Label usernameErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;

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

        setupErrorLabel(usernameErrorLabel);
        setupErrorLabel(phoneErrorLabel);
        setupErrorLabel(emailErrorLabel);
        setupErrorLabel(passwordErrorLabel);
        setupErrorLabel(confirmPasswordErrorLabel);

        usernameField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        validateUsername()
        );

        phoneField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        validatePhone()
        );

        emailField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        validateEmail()
        );

        passwordField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    validatePassword();

                    if (!confirmPasswordField.getText().isEmpty()) {
                        validateConfirmPassword();
                    }
                }
        );

        confirmPasswordField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        validateConfirmPassword()
        );
    }

    private void setupErrorLabel(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(!message.isEmpty());
        label.setManaged(!message.isEmpty());
    }

    private boolean validateUsername() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showError(usernameErrorLabel, "Username is required.");
            return false;
        }

        if (username.length() < 3 || username.length() > 30) {
            showError(usernameErrorLabel,
                    "Username must contain 3 to 30 characters.");
            return false;
        }

        if (!username.matches("[A-Za-z0-9._]+")) {
            showError(usernameErrorLabel,
                    "Only letters, numbers, dots and underscores are allowed.");
            return false;
        }

        showError(usernameErrorLabel, "");
        return true;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText().trim();

        if (phone.isEmpty()) {
            showError(phoneErrorLabel, "Phone number is required.");
            return false;
        }

        if (!phone.matches("[0-9]+")) {
            showError(phoneErrorLabel,
                    "Phone number must contain only digits.");
            return false;
        }

        if (phone.length() > 11) {
            showError(phoneErrorLabel,
                    "Phone number cannot exceed 11 digits.");
            return false;
        }

        if (!phone.matches("0[0-9]{9,10}")) {
            showError(phoneErrorLabel,
                    "Phone number must start with 0 and contain 10 or 11 digits.");
            return false;
        }

        showError(phoneErrorLabel, "");
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError(emailErrorLabel, "Email is required.");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError(emailErrorLabel,
                    "Please enter a valid email address.");
            return false;
        }

        showError(emailErrorLabel, "");
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();

        if (password.isEmpty()) {
            showError(passwordErrorLabel, "Password is required.");
            return false;
        }

        if (password.length() < 8) {
            showError(passwordErrorLabel,
                    "Password must contain at least 8 characters.");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            showError(passwordErrorLabel,
                    "Password must contain an uppercase letter.");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            showError(passwordErrorLabel,
                    "Password must contain a lowercase letter.");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            showError(passwordErrorLabel,
                    "Password must contain a number.");
            return false;
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            showError(passwordErrorLabel,
                    "Password must contain a special character.");
            return false;
        }

        if (password.matches(".*\\s.*")) {
            showError(passwordErrorLabel,
                    "Password cannot contain whitespace.");
            return false;
        }

        showError(passwordErrorLabel, "");
        return true;
    }

    private boolean validateConfirmPassword() {
        String confirmPassword = confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            showError(confirmPasswordErrorLabel,
                    "Please confirm your password.");
            return false;
        }

        if (!confirmPassword.equals(passwordField.getText())) {
            showError(confirmPasswordErrorLabel,
                    "Passwords do not match.");
            return false;
        }

        showError(confirmPasswordErrorLabel, "");
        return true;
    }


    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        boolean validUsername = validateUsername();
        boolean validPhone = validatePhone();
        boolean validEmail = validateEmail();
        boolean validPassword = validatePassword();
        boolean validConfirmPassword = validateConfirmPassword();

        if (!(validUsername && validPhone && validEmail
                && validPassword && validConfirmPassword)) {
            return;
        }

        String result = MockAuthService.register(
                usernameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                passwordField.getText()
        );

        if (result != null) {
            String error = result.toLowerCase();

            if (error.contains("username")) {
                showError(usernameErrorLabel, result);
            } else if (error.contains("phone")) {
                showError(phoneErrorLabel, result);
            } else if (error.contains("email")) {
                showError(emailErrorLabel, result);
            } else if (error.contains("password")) {
                showError(passwordErrorLabel, result);
            } else {
                showError(usernameErrorLabel, result);
            }
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
