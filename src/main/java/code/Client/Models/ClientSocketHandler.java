package code.Client.Models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

/**
 * A class responsible for handling client socket communication.
 */
public class ClientSocketHandler implements Runnable {
    private final SimpleBooleanProperty running;
    private final String address;
    private final SimpleBooleanProperty connected;
    private final ArrayList<Email> emails;
    private final Email[] focusedEmails;
    private final int page;
    private final SimpleStringProperty[] recentContacts;

    /**
     * Constructs a ClientSocketHandler with the specified parameters.
     *
     * @param running        a SimpleBooleanProperty indicating if the client socket handler is running
     * @param address        the address of the server
     * @param connected      a SimpleBooleanProperty indicating if the client is connected to the server
     * @param emails         the list of emails
     * @param focusedEmails  an array of focused emails
     * @param page           the current page number
     * @param recentContacts an array of recent contacts
     */
    public ClientSocketHandler(SimpleBooleanProperty running, String address, SimpleBooleanProperty connected, ArrayList<Email> emails, Email[] focusedEmails, int page, SimpleStringProperty[] recentContacts) {
        this.running = running;
        this.address = address;
        this.connected = connected;
        this.emails = emails;
        this.focusedEmails = focusedEmails;
        this.page = page;
        this.recentContacts = recentContacts;
    }

    /**
     * The main run method of the client socket handler.
     * It continuously checks for connectivity with the server and pings the server at regular intervals.
     */
    @Override
    public void run() {
        while (running.get()) {
            if (!connected.get()) {
                connectToServer();
            }
            pingServer();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted", e);
            }
        }
        if (connected.get()) {
            disconnect();
        }
    }

    /**
     * Connects to the server using a new thread.
     */
    private void connectToServer() {
        Runnable r = new ClientSocketThread("connect", address, connected, null, null, 0, null);
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Pings the server using a new thread.
     */
    private void pingServer() {
        Runnable r = new ClientSocketThread("ping", address, connected, emails, focusedEmails, page, recentContacts);
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Disconnects from the server using a new thread.
     */
    private void disconnect() {
        Runnable r = new ClientSocketThread("disconnect", address, connected, null, null, 0, null);
        Thread t = new Thread(r);
        t.start();
    }
}
