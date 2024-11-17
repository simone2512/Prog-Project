package code.Client.Models;

import code.SocketUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A thread class for handling client socket communication.
 */
public class ClientSocketThread extends SocketUtils implements Runnable {
    private final String action;
    private final String address;
    private final SimpleBooleanProperty connected;
    private ArrayList<Email> emails;
    private Email[] focusedEmails;
    private int page;
    private SimpleStringProperty[] recentContacts;
    private Email email = null;

    /**
     * Constructs a ClientSocketThread with the specified parameters for actions like connect or send.
     *
     * @param action    the action to be performed
     * @param address   the address of the server
     * @param connected a SimpleBooleanProperty indicating if the client is connected to the server
     * @param email     the email to be sent (if applicable)
     */
    public ClientSocketThread(String action, String address, SimpleBooleanProperty connected, Email email) {
        super();
        this.action = action;
        this.address = address;
        this.connected = connected;
        this.email = email;
    }

    /**
     * Constructs a ClientSocketThread with the specified parameters for actions like ping.
     *
     * @param action         the action to be performed
     * @param address        the address of the server
     * @param connected      a SimpleBooleanProperty indicating if the client is connected to the server
     * @param emails         the list of emails (if applicable)
     * @param focusedEmails an array of focused emails (if applicable)
     * @param page           the current page number (if applicable)
     * @param recentContacts an array of recent contacts (if applicable)
     */
    public ClientSocketThread(String action, String address, SimpleBooleanProperty connected, ArrayList<Email> emails, Email[] focusedEmails, int page, SimpleStringProperty[] recentContacts) {
        super();
        this.action = action;
        this.address = address;
        this.connected = connected;
        this.emails = emails;
        this.focusedEmails = focusedEmails;
        this.page = page;
        this.recentContacts = recentContacts;
    }

    /**
     * The main run method of the client socket thread.
     * It switches between different actions like connect, ping, send, or disconnect.
     */
    @Override
    public void run() {
        switch (action) {
            case "connect":
                connect();
                break;
            case "ping":
                pingServer();
                break;
            case "send":
                sendEmail();
                break;
            case "disconnect":
                disconnect();
                break;
            default:
                System.out.println("Action not recognized " + action);
                break;
        }
    }

    /**
     * Connects to the server.
     * If the connection is successful, it sends the server the address and action, then waits for a response.
     * Finally, it closes the socket.
     */
    private void connect() {
        connectToServer();
        if (!connected.get()) {
            return;
        }
        response();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Establishes a connection to the server and sends a request with the given address and action.
     * If the connection is successful, sets the connected property to true.
     * If the connection fails, sets the connected property to false.
     */
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 3000);
            this.connected.set(true);
            writeSocket(address + "$" + action);
        } catch (IOException e) {
            this.connected.set(false);
        }
    }

    /**
     * Establishes a connection to the server and sends a ping request.
     * Upon receiving a response, it processes the emails and updates the UI.
     * Finally, it closes the socket.
     */
    private void pingServer() {
        connectToServer();
        if (!connected.get()) {
            return;
        }
        String line = response();
        if (line != null) {
            addEmails(line);
        }
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the server response and performs appropriate actions based on the response.
     *
     * @return the response received from the server
     */
    private String response() {
        String line = readSocket();
        if (line.equals("$ok")) {
            return null;
        }
        if (line.equals("$over")) {
            try {
                socket.close();
            } catch (IOException ignored) {}
            connected.set(false);
        }
        if (line.equals("$error")) {
            System.out.println("Bad response");
            return null;
        }
        return line;
    }

    /**
     * Parses the JSON string containing emails, adds them to the local list, and updates UI components.
     *
     * @param line the JSON string received from the server
     */
    private void addEmails(String line) {
        JSONArray jsonArray = new JSONArray(line);
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonEmail = new JSONObject(jsonArray.getString(i));
                Email email = new Email(jsonEmail);
                if (email.getId() == 0 || !emails.contains(email))
                    this.emails.addFirst(email);
                Platform.runLater(() -> {
                    updateRecentContacts(email.getSender());
                    EmailInbox.updateFocusedEmails(focusedEmails, page, emails);
                });
            } catch (JSONException e) {
                throw new RuntimeException("Cannot parse email", e);
            }
        }
    }

    /**
     * Updates the recent contacts list with the sender of the received email.
     *
     * @param sender the sender of the email
     */
    private void updateRecentContacts(SimpleStringProperty sender) {
        int existingIndex = -1;
        for (int i = 0; i < recentContacts.length; i++) {
            if (Objects.equals(recentContacts[i].get(), sender.get())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex != -1) {
            for (int i = existingIndex; i > 0; i--) {
                recentContacts[i].set(recentContacts[i - 1].get());
            }
            recentContacts[0].set(sender.get());
        } else {
            for (int i = recentContacts.length - 1; i > 0; i--) {
                recentContacts[i].set(recentContacts[i - 1].get());
            }
            recentContacts[0].set(sender.get());
        }
    }

    /**
     * Sends an email to the server.
     * Upon sending, it waits for a response from the server and then closes the socket.
     */
    private void sendEmail() {
        connectToServer();
        if (!connected.get()) {
            return;
        }
        response();
        writeSocket(email.toJSON());
        response();
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Initiates the disconnection process from the server.
     * It first connects to the server, then sets the connected property to false.
     */
    public void disconnect() {
        connect();
        connected.set(false);
    }
}
