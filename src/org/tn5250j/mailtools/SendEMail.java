/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.mailtools;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class SendEMail {

    private String to;
    private String from;
    private String pers;
    private String cc;
    private String subject;
    private String configFile;
    private String message;
    private String attachment;
    private String attachmentName;
    private String fileName;

    // SMTP Properties file
    Map<String, String> SMTPProperties;

    public void setTo(final String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setCC(final String cc) {
        this.cc = cc;
    }

    public String getCC() {
        return cc;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setConfigFile(final String file) {
        this.configFile = file;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setAttachment(final String text) {
        this.attachment = text;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setMessage(final String text) {
        this.message = text;
    }

    public String getMessage() {
        return message;
    }

    public void setAttachmentName(final String desc) {

        attachmentName = desc;
    }

    public String getAttachmentName() {

        return attachmentName;

    }

    public void setFileName(final String name) {
        this.fileName = name;
    }

    public String getFileName() {
        return fileName;
    }


    /**
     * <p>Loads the given configuration file.
     *
     * @param name Configuration file name
     * @return true if the configuration file was loaded
     */
    private boolean loadConfig(final String name) throws Exception {

        SMTPProperties = ConfigureFactory.getInstance().getProperties("smtp",
                "SMTPProperties.cfg");

        if (SMTPProperties.size() > 0)
            return true;
        else
            return false;
    }

    // clean-up -- this should be called by the JSP Container...
    public void release() {

        // clean up variables to be used the next time
        to = null;
        from = null;
        cc = null;
        subject = null;
        configFile = null;
        message = null;
        attachment = null;
        attachmentName = null;
        fileName = null;
    }

    /**
     * This method processes the send request from the compose form
     * @return true if successfully send, false otherwise
     * @throws Exception if failed to send.
     */
    public boolean send() throws Exception {

        try {
            if (!loadConfig(configFile))
                return false;

            final Properties props = new Properties();
            props.putAll(SMTPProperties);

            final Session session = Session.getDefaultInstance(props, null);
            session.setDebug(false);

            // create the Multipart and its parts to it
            final Multipart mp = new MimeMultipart();

            final Message msg = new MimeMessage(session);
            InternetAddress[] toAddrs = null, ccAddrs = null;

            toAddrs = InternetAddress.parse(to, false);
            msg.setRecipients(Message.RecipientType.TO, toAddrs);

            if (cc != null) {
                ccAddrs = InternetAddress.parse(cc, false);
                msg.setRecipients(Message.RecipientType.CC, ccAddrs);
            }

            if (subject != null)
                msg.setSubject(subject.trim());

            if (from == null)
                from = SMTPProperties.get("mail.smtp.from");

            if (from != null && from.length() > 0) {
                pers = SMTPProperties.get("mail.smtp.realname");
                if (pers != null) msg.setFrom(new InternetAddress(from, pers));
            }

            if (message != null && message.length() > 0) {
                // create and fill the attachment message part
                final MimeBodyPart mbp = new MimeBodyPart();
                mbp.setText(message, "us-ascii");
                mp.addBodyPart(mbp);
            }

            msg.setSentDate(new Date());

            if (attachment != null && attachment.length() > 0) {
                // create and fill the attachment message part
                final MimeBodyPart abp = new MimeBodyPart();

                abp.setText(attachment, "us-ascii");

                if (attachmentName == null || attachmentName.length() == 0)
                    abp.setFileName("tn5250j.txt");
                else
                    abp.setFileName(attachmentName);
                mp.addBodyPart(abp);

            }

            if (fileName != null && fileName.length() > 0) {
                // create and fill the attachment message part
                final MimeBodyPart fbp = new MimeBodyPart();

                fbp.setText("File sent using tn5250j", "us-ascii");

                if (attachmentName == null || attachmentName.length() == 0) {
                    fbp.setFileName("tn5250j.txt");
                } else
                    fbp.setFileName(attachmentName);

                // Get the attachment
                final DataSource source = new FileDataSource(fileName);

                // Set the data handler to the attachment
                fbp.setDataHandler(new DataHandler(source));

                mp.addBodyPart(fbp);

            }

            // add the Multipart to the message
            msg.setContent(mp);

            // send the message
            Transport.send(msg);
            return true;
        } catch (final SendFailedException sfe) {
            showFailedException(sfe);
        }
        return false;
    }

    /**
     * Show the error list from the e-mail API if there are errors
     *
     * @param parent
     * @param sfe
     */
    private static void showFailedException(final SendFailedException sfe) {

        String error = sfe.getMessage() + "\n";

        final Address[] ia = sfe.getInvalidAddresses();

        if (ia != null) {
            for (int x = 0; x < ia.length; x++) {
                error += "Invalid Address: " + ia[x].toString() + "\n";
            }
        }

        final Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(LangTool.getString("em.titleConfirmation"));

        final TextArea ea = new TextArea(error);
        ea.setPrefRowCount(6);
        ea.setPrefColumnCount(50);

        alert.setHeaderText("");
        alert.getDialogPane().setContent(ea);

        alert.showAndWait();
    }
}
