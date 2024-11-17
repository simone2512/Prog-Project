package code.Client.Controllers;

import code.Client.Models.EmailInbox;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class ClientController {
    private static final int INBOX = 0;
    private static final int WRITE = 1;
    private static final int READ = 2;

    @FXML private VBox parent;
    @FXML private TextArea area;
    @FXML private Button writeButton;
    @FXML private Button sendButton;
    @FXML private Pane writePane;
    @FXML private Pane inboxPane;
    @FXML private Pane readPane;
    @FXML private HBox firstContact;
    @FXML private Label firstContactLabel;
    @FXML private HBox secondContact;
    @FXML private Label secondContactLabel;
    @FXML private HBox thirdContact;
    @FXML private Label thirdContactLabel;
    @FXML private TextField recipientsText;
    @FXML private TextField subjectText;
    @FXML private HBox email1;
    @FXML private Label sender1;
    @FXML private Label subject1;
    @FXML private Line line1;
    @FXML private HBox email2;
    @FXML private Label sender2;
    @FXML private Label subject2;
    @FXML private Line line2;
    @FXML private HBox email3;
    @FXML private Label sender3;
    @FXML private Label subject3;
    @FXML private Line line3;
    @FXML private HBox email4;
    @FXML private Label sender4;
    @FXML private Label subject4;
    @FXML private Line line4;
    @FXML private HBox email5;
    @FXML private Label sender5;
    @FXML private Label subject5;
    @FXML private Line line5;
    @FXML private HBox email6;
    @FXML private Label sender6;
    @FXML private Label subject6;
    @FXML private Line line6;
    @FXML private ImageView alert;
    @FXML private TextField sendersText;
    @FXML private TextField readingSubjectText;
    @FXML private TextArea readingBody;
    
    private String address;

    private EmailInbox emailInbox;

    public void initialize(Stage stage, String address) {
        area.setWrapText(true);
        setState(INBOX);
        parent.requestFocus();
        this.address = address;
        emailInbox = new EmailInbox(address);

        stage.setOnCloseRequest(event -> emailInbox.close());

        bindContactLabel(firstContactLabel, firstContact, 0);
        bindContactLabel(secondContactLabel, secondContact, 1);
        bindContactLabel(thirdContactLabel, thirdContact, 2);

        bindEmail(email1, sender1, subject1, line1, 0);
        bindEmail(email2, sender2, subject2, line2, 1);
        bindEmail(email3, sender3, subject3, line3, 2);
        bindEmail(email4, sender4, subject4, line4, 3);
        bindEmail(email5, sender5, subject5, line5, 4);
        bindEmail(email6, sender6, subject6, line6, 5);

        sendersText.textProperty().bind(emailInbox.getFocusedEmail().getSender());
        readingSubjectText.textProperty().bind(emailInbox.getFocusedEmail().getSubject());
        readingBody.textProperty().bind(emailInbox.getFocusedEmail().getBody());

        alert.visibleProperty().bind(emailInbox.isConnected().not());
    }

    @FXML
    protected void writeClick() {
        setState(WRITE);
    }

    @FXML
    protected void sendClick() {
        String line = checkEmail();
        if (line != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Can't send email");
            alert.setHeaderText(null);
            alert.setContentText(line);
            alert.showAndWait();
            return;
        }
        emailInbox.sendEmail(recipientsText.textProperty().get(), subjectText.textProperty().get(), area.textProperty().get());
        emptyMail();
        setState(INBOX);
    }

    private String checkEmail() {
        if (!emailInbox.isConnected().get())
            return "Can't send email, not connected to server";
        if (recipientsText.textProperty().isEmpty().get()) {
            recipientsText.requestFocus();
            return "Recipients field can't be empty";
        }
        String[] recipients = recipientsText.textProperty().get().split(" ");
        String pattern = "^[a-zA-Z0-9.]+@email$";
        Pattern regexPattern = Pattern.compile(pattern);
        for (String recipient : recipients) {
            if (!regexPattern.matcher(recipient).matches()) {
                recipientsText.requestFocus();
                return "Invalid email address " + "'" + recipient + "'";
            }
        }
        if (subjectText.textProperty().isEmpty().get()) {
            subjectText.requestFocus();
            return "Subject field can't be empty";
        }
        if (area.textProperty().isEmpty().get()) {
            area.requestFocus();
            return "Email body can't be empty";
        }
        return null;
    }

    @FXML
    protected void xClick() {
        emptyMail();
        setState(INBOX);
    }

    @FXML
    protected void contactClick(Label label) {
        setState(WRITE);
        recipientsText.appendText(label.getText() + " ");
    }

    @FXML
    protected void firstContactClick() {
        contactClick(firstContactLabel);
    }

    @FXML
    protected void secondContactClick() {
        contactClick(secondContactLabel);
    }

    @FXML
    protected void thirdContactClick() {
        contactClick(thirdContactLabel);
    }

    @FXML
    protected void previousPage() {
        emailInbox.previousPage();
    }

    @FXML
    protected void nextPage() {
        emailInbox.nextPage();
    }

    private void bindContactLabel(Label label, HBox box, int index) {
        label.textProperty().bind(emailInbox.getRecentContact(index));
        box.visibleProperty().bind(label.textProperty().isNotEmpty());
    }

    private void bindEmail(HBox box, Label sender, Label subject, Line line, int index) {
        sender.textProperty().bind(emailInbox.getFocusedEmail(index).getSender());
        subject.textProperty().bind(emailInbox.getFocusedEmail(index).getSubject());
        box.visibleProperty().bind(sender.textProperty().isNotEmpty());
        line.visibleProperty().bind(sender.textProperty().isNotEmpty());
        HBox emailBody = (HBox) box.getChildren().getFirst();
        emailBody.onMouseClickedProperty().set(event -> {
            emailInbox.readEmail(index);
            setState(READ);
        });
        ObservableList<Node> emailButtons = ((HBox) box.getChildren().getLast()).getChildren();
        // Reply button
        emailButtons.getFirst().onMouseClickedProperty().set(event -> {
            emailInbox.readEmail(index);
            recipientsText.setText(emailInbox.getFocusedEmail().getSender().get());
            subjectText.setText("Re: " + emailInbox.getFocusedEmail().getSubject().get());
            setState(WRITE);
        });
        // Reply all button
        emailButtons.get(1).onMouseClickedProperty().set(event -> {
            emailInbox.readEmail(index);
            String groupChat = emailInbox.getFocusedEmail().getGroup().get() + " " + emailInbox.getFocusedEmail().getSender().get();
            groupChat = groupChat.replace(this.address + " ", "");
            recipientsText.setText( groupChat + " ");
            subjectText.setText("Re all: " + emailInbox.getFocusedEmail().getSubject().get());
            setState(WRITE);
        });
        // Forward button
        emailButtons.get(2).onMouseClickedProperty().set(event -> {
            emailInbox.readEmail(index);
            subjectText.setText("Fwd: " + emailInbox.getFocusedEmail().getSubject().get());
            area.setText("From " + emailInbox.getFocusedEmail().getSender().get() + " To " + emailInbox.getFocusedEmail().getGroup().get() + ":" + emailInbox.getFocusedEmail().getBody().get() + "");
            setState(WRITE);
        });
        // Delete button
        emailButtons.getLast().onMouseClickedProperty().set(event -> {
            emailInbox.deleteEmail(index);
            setState(INBOX);
        });
    }

    public void setState(int state) {
        switch (state) {
            case INBOX:
                readPane.setVisible(false);
                writePane.setVisible(false);
                writeButton.setVisible(true);
                inboxPane.setVisible(true);
                sendButton.setVisible(false);
                break;
            case WRITE:
                readPane.setVisible(false);
                writePane.setVisible(true);
                writeButton.setVisible(false);
                inboxPane.setVisible(false);
                sendButton.setVisible(true);
                break;
            case READ:
                writePane.setVisible(false);
                writeButton.setVisible(true);
                inboxPane.setVisible(false);
                sendButton.setVisible(false);
                readPane.setVisible(true);
                break;
        }
        parent.requestFocus();
    }

    public void emptyMail() {
        recipientsText.setText("");
        subjectText.setText("");
        area.setText("");
    }
}
