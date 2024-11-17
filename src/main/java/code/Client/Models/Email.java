package code.Client.Models;

import javafx.beans.property.SimpleStringProperty;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an email.
 */
public class Email implements Serializable {
    private int id;
    private final SimpleStringProperty sender;
    private final SimpleStringProperty recipient;
    private final SimpleStringProperty group;
    private final SimpleStringProperty subject;
    private final SimpleStringProperty body;
    private SimpleDateFormat date;


    /**
     * Constructs an Email with specified properties.
     *
     * @param id        the ID of the email
     * @param sender    the sender of the email
     * @param recipient the recipient of the email
     * @param group     the group of recipients for the email
     * @param subject   the subject of the email
     * @param body      the body of the email
     * @param date      the date of the email
     */
    public Email(int id, SimpleStringProperty sender, SimpleStringProperty recipient, SimpleStringProperty group, SimpleStringProperty subject, SimpleStringProperty body, SimpleDateFormat date) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    /**
     * Constructs an Email from a JSON object.
     *
     * @param jsonEmail the JSON representation of the email
     */
    public Email(JSONObject jsonEmail) {
        this.id = jsonEmail.getInt("id");
        this.sender = new SimpleStringProperty(jsonEmail.getString("sender"));
        this.recipient = new SimpleStringProperty(jsonEmail.getString("recipient"));
        this.group = new SimpleStringProperty(jsonEmail.getString("group"));
        this.subject = new SimpleStringProperty(jsonEmail.getString("subject"));
        this.body = new SimpleStringProperty(jsonEmail.getString("body"));
        this.date = new SimpleDateFormat(jsonEmail.getString("date"));
    }

    /**
     * Gets the ID of the email.
     *
     * @return the ID of the email
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the email.
     *
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the sender of the email.
     *
     * @return the sender of the email
     */
    public SimpleStringProperty getSender() {
        return sender;
    }

    /**
     * Gets the recipient of the email.
     *
     * @return the recipient of the email
     */
    public SimpleStringProperty getRecipient() {
        return recipient;
    }

    /**
     * Gets the group of the email.
     *
     * @return the group of the email
     */
    public SimpleStringProperty getGroup() {
        return group;
    }

    /**
     * Gets the subject of the email.
     *
     * @return the subject of the email
     */
    public SimpleStringProperty getSubject() {
        return subject;
    }

    /**
     * Gets the body of the email.
     *
     * @return the body of the email
     */
    public SimpleStringProperty getBody() {
        return body;
    }

    /**
     * Gets the date of the email.
     *
     * @return the date of the email
     */
    public SimpleDateFormat getDate() {
        return date;
    }

    /**
     * Sets the properties of the email based on another email object.
     *
     * @param email the email object to copy properties from
     */
    public void set(Email email) {
        this.id = email.getId();
        this.sender.set(email.getSender().get());
        this.recipient.set(email.getRecipient().get());
        this.group.set(email.getGroup().get());
        this.subject.set(email.getSubject().get());
        this.body.set(email.getBody().get());
        this.date = email.getDate();
    }

    /**
     * Converts the email object to its JSON representation.
     *
     * @return the JSON representation of the email
     */
    public String toJSON() {
        return "{\"id\":\"" + id + "\",\"sender\":\"" + sender.get() + "\",\"recipient\":\"" + recipient.get() + "\",\"group\":\"" + group.get() + "\",\"subject\":\"" + subject.get() + "\",\"body\":\"" + body.get() + "\",\"date\":\"" + date.format(new Date()) + "\"}";
    }

    /**
     * Checks if this email is equal to another object.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Email email)) {
            return false;
        }
        return email.getId() == this.id;
    }
}
