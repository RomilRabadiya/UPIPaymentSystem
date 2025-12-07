package com.example.UPIPaymentSystem.AuthenticationServices;
//using :

//JavaMailSender
//JavaMailSender
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


//We can Use JavaMailSender Service Built in service in Spring Boot to send email

//Spring Boot (JavaMailSender) sends your email to your configured SMTP server
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
//        An SMTP provider is a service that uses the Simple Mail Transfer Protocol (SMTP) to send emails on behalf of users or applications
        mailSender.send(message);
    }
    
    
    public void sendTransactionEmail(
            String to,
            String username,
            String transactionType,   // BankTransfer, UPI, Contact, QRCode
            String sender,
            String receiver,
            String amount,
            String txnId
    ) {
            // Mask account number if it's numeric
            String maskedSender = maskIfAccount(sender);
            String maskedReceiver = maskIfAccount(receiver);

            String subject = transactionType + " Successful - UPI Payment System";

            String text =
                    "Hello " + username + ",\n\n" +
                    "Your " + transactionType + " has been successfully completed.\n\n" +
                    "-----------------------------------------\n" +
                    "Transaction Type : " + transactionType + "\n" +
                    "Amount           : ₹" + amount + "\n" +
                    "Sender           : " + maskedSender + "\n" +
                    "Receiver         : " + maskedReceiver + "\n" +
                    "Transaction ID   : " + txnId + "\n" +
                    "Status           : SUCCESS\n" +
                    "-----------------------------------------\n\n" +
                    "If this was not you, please contact your bank immediately.\n\n" +
                    "Thank you for using UPI Payment System!\n";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
    }
    
    private String maskIfAccount(String value) {
        // If it only contains digits → treat as account number
        if (value != null && value.matches("\\d+")) {
            int len = value.length();

            if (len <= 4) return value; // nothing to mask

            String last4 = value.substring(len - 4);
            return "XXXXXX" + last4;
        }

        // Otherwise treat as UPI and return normally
        return value;
    }

}