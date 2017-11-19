package com.mycompany.service;

import com.google.inject.Inject;
import com.mycompany.domain.shop.Order;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final static Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Inject
    private Config config;

    private String placedBody = "Размещен новый заказ, перейти к обработке заказа %sadmin/arm/%s";

    public void send(String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getString("email.host"));
        props.put("mail.smtp.socketFactory.port", config.getString("email.port"));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", config.getString("email.port"));
        props.put("mail.smtp.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.localhost", "127.0.0.1");

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getString("email.login"), config.getString("email.pass"));
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getString("email.login")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.getString("email.notification")));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Error sending email", e);
        }
    }

    public void sendOrderPlacedEmail(Order order) {
        send("Новый заказ №" + order.orderNumber,
                String.format(placedBody, config.getString("application.externalUrl"), order.orderNumber));
    }
}
