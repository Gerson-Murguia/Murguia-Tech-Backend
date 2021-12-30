package com.example.securityjwt.service;

import com.sun.mail.smtp.SMTPTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import static com.example.securityjwt.constant.EmailConstant.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    //TODO: Mandarlo asincrono
    public void sendNewPasswordEmail(String firstName, String password, String email) throws MessagingException {
        Message message = createEmail(firstName,password,email);
        SMTPTransport smtpTransport= (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER,USERNAME,PASSWORD);
        smtpTransport.sendMessage(message,message.getAllRecipients());
        smtpTransport.close();
    }

    public void sendUnlockEmail(){

    }
    private Session getEmailSession(){
        Properties properties=System.getProperties();
        properties.put(SMTP_HOST,GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH,true);
        properties.put(SMTP_PORT,DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE,true);
        properties.put(Smtp_STARTTLS_REQUIRED,true);
        return Session.getInstance(properties,null);
    }

    private Message createEmail(String firstName,String password,String email) throws MessagingException {
        Message message=new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(email));
        message.setRecipients(Message.RecipientType.CC,InternetAddress.parse(CC_EMAIL));
        message.setSubject(EMAIL_SUBJECT);
        message.setSentDate(new Date());
        message.saveChanges();
        message.setText("Hello "+firstName+", \n\n La nueva contraseña es: "+password+"\n\n Equipo de Soporte - Murguia Tech");
        return message;
    }
}
