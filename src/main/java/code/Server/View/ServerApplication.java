package code.Server.View;

import code.Server.Controllers.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("serverInterface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Email Server");
        stage.setScene(scene);
        stage.setResizable(false);
        ServerController controller = fxmlLoader.getController();
        controller.initialize(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
