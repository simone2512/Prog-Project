package code.Client.View;

import code.Client.Controllers.LogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("logInterface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/stylesheets/logStyle.css")).toExternalForm());
        stage.setTitle("Email!");
        stage.setScene(scene);
        stage.setResizable(false);
        LogController controller = fxmlLoader.getController();
        controller.initialize(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
