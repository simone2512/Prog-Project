package code.Client.Controllers;

import code.Client.View.ClientApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogController {

    private Stage stage;

    @FXML
    AnchorPane parent;

    @FXML
    TextField emailText;

    @FXML
    Label errorLabel;

    @FXML
    Button enterButton;

    public void initialize(Stage stage) {
        parent.requestFocus();
        this.stage = stage;
    }

    @FXML
    public void enterClick() {
        logIn();
    }

    @FXML
    private void logTextEnter(javafx.scene.input.KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            logIn();
        }
    }

    private void logIn() {
        String address = emailText.getText();
        String error = check(address);
        if (error != null) {
            emailText.setStyle("-fx-border-color: red; -fx-background-color: #a5dbe6");
            errorLabel.setText(error);
            errorLabel.setVisible(true);
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("clientInterface.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 600, 400);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/stylesheets/clientStyle.css")).toExternalForm());
                stage.setTitle(address);
                stage.setScene(scene);
                stage.setResizable(false);
                ClientController controller = fxmlLoader.getController();
                controller.initialize(stage, address);
                stage.show();
            }
            catch (IOException e) {
                throw new RuntimeException("Cannot load client interface", e);
            }
        }
    }

    public static String check(String email) {
        String pattern = "^[a-zA-Z0-9.]+@email$";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(email);

        if (matcher.matches()) {
            return null;
        } else {
            if (!email.matches("^[a-zA-Z0-9.]+")) {
                return "Email can only contain alphanumeric characters or dots.";
            } else if (!email.endsWith("@email")) {
                return "Email must end with '@email'.";
            } else {
                return "Invalid email address.";
            }
        }
    }
}
