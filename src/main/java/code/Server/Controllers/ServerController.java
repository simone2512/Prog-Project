package code.Server.Controllers;

import code.Server.Models.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ServerController {

    @FXML
    AnchorPane parent;
    @FXML
    ToggleButton powerButton;
    @FXML
    Label activeUsers;
    @FXML
    TextFlow log;
    private Server server;

    public void changePower() {
        if (powerButton.isSelected()) {
            server.off();
            powerButton.setText("POWER ON");
        } else {
            server.on();
            powerButton.setText("POWER OFF");
        }
    }

    public void initialize(Stage stage) {
        parent.requestFocus();
        server = new Server();

        stage.setOnCloseRequest(event -> server.off());

        activeUsers.textProperty().bind(server.getActiveUsers());
        Text logs = new Text();
        logs.textProperty().bind(server.getLogs());
        log.getChildren().add(logs);
    }
}
