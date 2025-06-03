import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * The EmailService is responsible for sending emails with file attachments
 * using a Gmail account or other SMTP service.
 *
 * Requires JavaMail API + Activation (javax.activation) in classpath.
 * Note: Gmail must allow app access or use an App Password.
 *
 * @author Van Bao Han Quach
 * @version June 3, 2025
 */
public class EmailService {

    /** Email address used to send emails (sender account). */
    private final String mySenderEmail;

    /** App-specific password or email password (if allowed). */
    private final String mySenderPassword;

    /**
     * Constructs an EmailService with sender credentials.
     *
     * @param theSenderEmail sender's email address
     * @param theSenderPassword app password or login password
     */
    public EmailService(String theSenderEmail, String theSenderPassword) {
        this.mySenderEmail = theSenderEmail;
        this.mySenderPassword = theSenderPassword;
    }

    /**
     * Sends an email with an attachment to a recipient.
     *
     * @param theRecipient recipient email address
     * @param theSubject subject line of the email
     * @param theMessage body of the email
     * @param theAttachment file to attach (e.g., CSV export)
     * @throws MessagingException if email sending fails
     * @throws IOException if attachment can't be read
     */
    public void sendEmailWithAttachment(String theRecipient, String theSubject,
                                        String theMessage, File theAttachment)
            throws MessagingException, IOException {

        // Set mail properties for Gmail SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Create session with authentication
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mySenderEmail, mySenderPassword);
            }
        });

        // Compose message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mySenderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(theRecipient));
        message.setSubject(theSubject);

        // Email body
        BodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(theMessage);

        // Attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(theAttachment);

        // Combine into multipart
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textBodyPart);
        multipart.addBodyPart(attachmentPart);

        // Set content and send
        message.setContent(multipart);
        Transport.send(message);
    }
}
