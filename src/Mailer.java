import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

class Mailer {

    static boolean sendMail (String to, String subject, String body) {

        String host = "smtp.gmail.com";
        int port = 465;
        String fromId = "watcher@animus.com";
        String fromName = "Watcher";
        String mUsername = "watcher.animus652@gmail.com";
        String mPassword = "1337ub3r";
        String charset = StandardCharsets.UTF_8.toString();
        String bodyType = "html";  // html or plain

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.enable", true);
        props.put("mail.smtp.auth", true);

        Authenticator authenticator = new Authenticator() {
            private PasswordAuthentication pa = new PasswordAuthentication(mUsername, mPassword);
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return pa;
            }
        };

        Session session = Session.getInstance(props, authenticator);
        session.setDebug(true);

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(fromId, fromName));
            InternetAddress[] recipients = {new InternetAddress(to)};
            message.setRecipients(Message.RecipientType.TO, recipients);
            message.setSubject(subject, charset);
            message.setSentDate(new Date());
            message.setText(body, charset, bodyType);
            Transport.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
