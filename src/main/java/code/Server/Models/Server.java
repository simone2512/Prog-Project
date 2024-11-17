package code.Server.Models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a server.
 */
public class Server {

    private ServerSocketHandler socketHandler;

    private final SimpleStringProperty activeUsers;
    private final SimpleStringProperty logs;
    private final SimpleBooleanProperty running;

    /**
     * Constructs a server.
     */
    public Server() {
        activeUsers = new SimpleStringProperty("");
        logs = new SimpleStringProperty("");
        running = new SimpleBooleanProperty(false);
    }

    /**
     * Turns on the server.
     */
    public void on() {
        running.set(true);
        socketHandler = new ServerSocketHandler(running, activeUsers, logs);
        Thread t = new Thread(socketHandler);
        t.start();
    }

    /**
     * Turns off the server.
     */
    public void off() {
        if(running.get()) {
            running.set(false);
            activeUsers.set("");
            logs.set("");
            socketHandler.stopServer();
        }
    }

    /**
     * Gets the property containing active users.
     *
     * @return the property of active users
     */
    public SimpleStringProperty getActiveUsers() {
        return activeUsers;
    }

    /**
     * Gets the property containing server logs.
     *
     * @return the property of server logs
     */
    public SimpleStringProperty getLogs() {
        return logs;
    }
}
