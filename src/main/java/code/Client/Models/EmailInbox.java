package code.Client.Models;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents an email inbox.
 */
public class EmailInbox {
    private final SimpleBooleanProperty running;
    private final String address;
    private final ArrayList<Email> emails;
    private final Email[] focusedEmails;
    private final Email focusedEmail;
    private int page;
    private final SimpleStringProperty[] recentContacts;
    private final SimpleBooleanProperty connected;

    /**
     * Constructs an EmailInbox with the specified address.
     *
     * @param address the address of the email inbox
     */
    public EmailInbox(String address) {
        this.running = new SimpleBooleanProperty(true);
        this.address = address;
        this.connected = new SimpleBooleanProperty(false);
        this.focusedEmails = new Email[6];
        this.focusedEmail = new Email(-1, new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleDateFormat());
        this.page = 0;
        this.emails = new ArrayList<>();
        this.recentContacts = new SimpleStringProperty[3];

        for (int i = 0; i < focusedEmails.length; i++)
            focusedEmails[i] = new Email(-1, new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleDateFormat());

        for (int i = 0; i < recentContacts.length; i++)
            recentContacts[i] = new SimpleStringProperty("");

        Runnable r = new ClientSocketHandler(running, address, connected, emails, focusedEmails, page, recentContacts);
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * Gets the property indicating whether the inbox is connected to the server.
     *
     * @return the property indicating connection status
     */
    public SimpleBooleanProperty isConnected() {
        return connected;
    }

    /**
     * Closes the inbox.
     */
    public void close() {
        this.running.set(false);
    }

    /**
     * Gets the recent contact at the specified index.
     *
     * @param index the index of the recent contact
     * @return the property of the recent contact
     */
    public SimpleStringProperty getRecentContact(int index) {
        return recentContacts[index];
    }

    /**
     * Gets the focused email at the specified index.
     *
     * @param index the index of the focused email
     * @return the focused email
     */
    public Email getFocusedEmail(int index) {
        return focusedEmails[index];
    }

    /**
     * Moves to the previous page of emails.
     */
    public void previousPage() {
        if (page > 0)
            page--;
        updateFocusedEmails(focusedEmails, page, emails);
    }

    /**
     * Moves to the next page of emails.
     */
    public void nextPage() {
        if (page < (emails.size() - 1) / 6)
            page++;
        updateFocusedEmails(focusedEmails, page, emails);
    }

    /**
     * Updates the focused emails based on the current page and email list.
     *
     * @param focusedEmails the array of focused emails
     * @param page           the current page
     * @param emails         the list of emails
     */
    static void updateFocusedEmails(Email[] focusedEmails, int page, ArrayList<Email> emails) {
        for (int i = 0; i < focusedEmails.length; i++) {
            if (i + page * 6 < emails.size() && emails.get(i + page * 6) != null)
                focusedEmails[i].set(emails.get(i + page * 6));
            else
                focusedEmails[i].set(new Email(0, new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleStringProperty(""), new SimpleDateFormat()));
        }
    }

    /**
     * Sends an email with the specified recipients, subject, and body.
     *
     * @param recipients the recipients of the email
     * @param subject    the subject of the email
     * @param body       the body of the email
     */
    public void sendEmail(String recipients, String subject, String body) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ArrayList<String> recipientsList = new ArrayList<>();
        for (String recipient : recipients.split(" ")) {
            if (recipient.isEmpty() || recipientsList.contains(recipient))
                continue;
            recipientsList.add(recipient);
            Email email = new Email(0, new SimpleStringProperty(address), new SimpleStringProperty(recipient), new SimpleStringProperty(recipients), new SimpleStringProperty(subject), new SimpleStringProperty(body), new SimpleDateFormat(now.format(formatter)));
            Runnable r = new ClientSocketThread("send", address, connected, email);
            Thread t = new Thread(r);
            t.start();
        }
    }

    public void readEmail(int index) {
        if (index < 0 || index >= focusedEmails.length || focusedEmails[index] == null)
            return;
        focusedEmail.set(focusedEmails[index]);
    }

    public Email getFocusedEmail() {
        return focusedEmail;
    }

    public void deleteEmail(int index) {
        if (index < 0 || index >= focusedEmails.length || focusedEmails[index] == null)
            return;
        int id = focusedEmails[index].getId();
        for (int i = 0; i < emails.size(); i++) {
            if (emails.get(i).getId() == id) {
                emails.remove(i);
                break;
            }
        }
        updateFocusedEmails(focusedEmails, page, emails);
    }
}
