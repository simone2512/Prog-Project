package code.Server.Models;

import code.Client.Models.Email;
import code.SocketUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Handles communication with a client socket.
 */
public class ServerSocketThread extends SocketUtils implements Runnable {
    private final Path EMAILS_PATH = Paths.get("C:\\Users\\simon\\OneDrive\\Desktop\\EmailApp\\EmailApp\\src\\main\\resources\\Data\\Emails.json");
    private final Semaphore semaphore;
    private final SimpleStringProperty activeUsers;
    private final SimpleStringProperty logs;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Email>> emailsMap;

    private String clientAddress;

    /**
     * Constructs a ServerSocketThread.
     *
     * @param socket     the client socket
     * @param semaphore  the semaphore
     * @param emailsMap  the map of emails
     * @param activeUsers the property containing active users
     * @param logs       the property containing server logs
     */
    public ServerSocketThread(Socket socket, Semaphore semaphore, ConcurrentHashMap<String, ConcurrentLinkedQueue<Email>> emailsMap, SimpleStringProperty activeUsers, SimpleStringProperty logs) {
        super(socket);
        this.socket = socket;
        this.semaphore = semaphore;
        this.emailsMap = emailsMap;
        this.activeUsers = activeUsers;
        this.logs = logs;
        this.clientAddress = "";
    }

    /**
     * Runs the server thread.
     */
    @Override
    public void run() {
        try {
            InputStream inStream = socket.getInputStream();
            Scanner in = new Scanner(inStream);
            String line = in.nextLine();
            clientAddress = line.split("\\$")[0];
            String action = line.split("\\$")[1];
            if (!clientAddress.matches(".*@email$")) {
                System.out.println("Invalid email address");
            }
            switch (action) {
                case "connect":
                    setUserOnline();
                    break;
                case "ping":
                    pingServer();
                    break;
                case "send":
                    sendEmail();
                    break;
                case "disconnect":
                    setUserOffline();
                    break;
                default:
                    System.out.println("Action not recognized " + action);
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Scanner error", e);
        }
    }

    /**
     * Sets the user online.
     */
    private void setUserOnline() {
        String content;
        try {
            content = new String(Files.readAllBytes(EMAILS_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read database", e);
        }
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            Email email = new Email(new JSONObject(jsonArray.getString(i)));
            if (Objects.equals(email.getRecipient().get(), clientAddress))
                emailsMap.get(email.getRecipient().get()).add(email);
        }
        Platform.runLater(() -> activeUsers.set(activeUsers.get() + "\n" + clientAddress));
        Platform.runLater(() -> pushLog("Connected"));
        writeSocket("$ok");
    }

    /**
     * Pings the server.
     */
    private void pingServer() {
        if (emailsMap == null) {
            writeSocket("$err");
            return;
        }
        if (!emailsMap.containsKey(clientAddress)) {
            emailsMap.put(clientAddress, new ConcurrentLinkedQueue<>());
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            emailsMap.get(clientAddress).add(new Email(0, new SimpleStringProperty("Team Email"), new SimpleStringProperty(clientAddress), new SimpleStringProperty(clientAddress), new SimpleStringProperty("Welcome to Email!"), new SimpleStringProperty("Welcome to the best email app in the world!"), new SimpleDateFormat(now.format(formatter))));
            writeSocket("$ok");
            return;
        }
        JSONArray jsonArray = new JSONArray();
        while (!emailsMap.get(clientAddress).isEmpty()) {
            Email email = emailsMap.get(clientAddress).poll();
            if (email != null) {
                jsonArray.put(email.toJSON());
            }
        }
        if (!jsonArray.isEmpty()) {
            writeSocket(jsonArray.toString());
        }
        writeSocket("$ok");
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Sends an email.
     */
    private void sendEmail() {
        writeSocket("$ok");
        String line = readSocket();
        Email email = new Email(new JSONObject(line));
        try {
            semaphore.acquire();
            int id = maxEmailID() + 1;
            if (id == -1) {
                semaphore.release();
                throw new RuntimeException("Error getting max email ID");
            }
            email.setId(id);
            if (emailsMap.containsKey(email.getRecipient().getValue())) {
                emailsMap.get(email.getRecipient().getValue()).add(email);
                saveEmail(email);
                pushLog("sent email " + email.getId() + " to " + email.getRecipient().getValue());
            }
            else {
                pushLog("failed to send an email, " + email.getRecipient().getValue() + " not found");
                Email res = new Email(0, new SimpleStringProperty("Team Email"), new SimpleStringProperty(clientAddress), new SimpleStringProperty(clientAddress), new SimpleStringProperty("Failed to send email"), new SimpleStringProperty(email.getRecipient().getValue() + " does not exist"), new SimpleDateFormat());
                emailsMap.get(clientAddress).add(res);
            }
            semaphore.release();
            writeSocket("$ok");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the maximum email ID from the stored emails.
     *
     * @return the maximum email ID
     */
    private int maxEmailID() {
        String content;
        int maxID = 0;
        try {
            content = new String(Files.readAllBytes(EMAILS_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read database", e);
        }
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonEmail = new JSONObject(jsonArray.getString(i));
            Email email = new Email(jsonEmail);
            if (email.getId() > maxID) {
                maxID = email.getId();
            }
        }
        return maxID;
    }

    /**
     * Saves the email to the storage.
     *
     * @param email the email to save
     */
    private void saveEmail(Email email) {
        String content;
        try {
            content = new String(Files.readAllBytes(EMAILS_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read database", e);
        }
        JSONArray jsonArray = new JSONArray(content);
        jsonArray.put(email.toJSON());
        try {
            Files.write(EMAILS_PATH, jsonArray.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the user offline.
     */
    private void setUserOffline() {
        String users = activeUsers.get();
        users = users.replace("\n" + clientAddress, "");
        String finalUsers = users;
        Platform.runLater(() -> activeUsers.set(finalUsers));
        Platform.runLater(() -> pushLog("Disconnected"));
        writeSocket("$ok");
    }

    /**
     * Pushes a log entry.
     *
     * @param log the log entry
     */
    private void pushLog(String log) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        logs.set(logs.get() + "\n" + formattedDateTime + " - " + clientAddress + " " + log + "\n");
    }
}
