package code.Server.Models;

import code.Client.Models.Email;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Handles incoming connections on the server socket.
 */
public class ServerSocketHandler implements Runnable {
    private final Path EMAILS_PATH = Paths.get("C:\\Users\\simon\\OneDrive\\Desktop\\EmailApp\\EmailApp\\src\\main\\resources\\Data\\Emails.json");
    private final ServerSocket serverSocket;
    private final SimpleBooleanProperty running;
    private final Semaphore emails_semaphore;
    private final SimpleStringProperty activeUsers;
    private final SimpleStringProperty logs;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Email>> emailsMap;

    /**
     * Constructs a ServerSocketHandler.
     *
     * @param running     the property indicating whether the server is running
     * @param activeUsers the property containing active users
     * @param logs        the property containing server logs
     */
    public ServerSocketHandler(SimpleBooleanProperty running, SimpleStringProperty activeUsers, SimpleStringProperty logs) {
        try {
            serverSocket = new ServerSocket(3000);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create server socket", e);
        }
        this.activeUsers = activeUsers;
        this.logs = logs;
        emails_semaphore = new Semaphore(1);
        this.running = running;
        emailsMap = new ConcurrentHashMap<>();
        loadEmails();
    }

    /**
     * Loads emails from a file.
     */
    private void loadEmails() {
        String content;
        try {
            content = new String(Files.readAllBytes(EMAILS_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read database", e);
        }
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            Email email = new Email(new JSONObject(jsonArray.getString(i)));
            if (!emailsMap.containsKey(email.getRecipient().get())) {
                emailsMap.put(email.getRecipient().get(), new ConcurrentLinkedQueue<>());
            }
            emailsMap.get(email.getRecipient().get()).add(email);
        }
    }

    /**
     * Stops the server socket.
     */
    public void stopServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Accepts incoming connections and starts a new thread to handle each connection.
     */
    @Override
    public void run() {
        try {
            while (running.get()) {
                Socket incoming = serverSocket.accept();
                Runnable r = new ServerSocketThread(incoming, emails_semaphore, emailsMap, activeUsers, logs);
                Thread t = new Thread(r);
                t.start();
            }
        } catch (Exception ignored) {} // ignore, this is normal when stopping the server
    }
}
