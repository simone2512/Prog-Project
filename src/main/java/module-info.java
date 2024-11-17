module code {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    exports code.Client.Controllers;
    opens code.Client.Controllers to javafx.fxml;
    exports code.Server.Controllers;
    opens code.Server.Controllers to javafx.fxml;
    exports code.Client.View;
    opens code.Client.View to javafx.fxml;
    exports code.Server.View;
    opens code.Server.View to javafx.fxml;
}