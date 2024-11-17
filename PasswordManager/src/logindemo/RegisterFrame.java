package logindemo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class RegisterFrame extends JFrame {

    private final JTextField usernameField, emailField;
    private final JPasswordField passwordField, RpasswordField;
    private final JButton registerButton, backButton;

    public RegisterFrame() {
        setTitle("Register");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null); // Set layout to null for manual positioning

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(150, 50, 100, 25);
        panel.add(usernameLabel);

        usernameField = new JTextField(15);
        usernameField.setBounds(270, 50, 150, 25);
        panel.add(usernameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(150, 90, 100, 25);
        panel.add(emailLabel);

        emailField = new JTextField(15);
        emailField.setBounds(270, 90, 150, 25);
        panel.add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(150, 130, 100, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField(15);
        passwordField.setBounds(270, 130, 150, 25);
        panel.add(passwordField);

        JLabel RpasswordLabel = new JLabel("Repeat Password:");
        RpasswordLabel.setBounds(150, 170, 120, 25);
        panel.add(RpasswordLabel);

        RpasswordField = new JPasswordField(15);
        RpasswordField.setBounds(270, 170, 150, 25);
        panel.add(RpasswordField);

        registerButton = new JButton("Register");
        registerButton.setBounds(150, 220, 100, 30);
        panel.add(registerButton);

        backButton = new JButton("Back");
        backButton.setBounds(320, 220, 100, 30);
        panel.add(backButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String repeatPassword = new String(RpasswordField.getPassword());

                if (!password.equals(repeatPassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match!");
                    return;
                }

                try {
                    insertData(username, email, password);
                    sendEmail(email, username);
                    JOptionPane.showMessageDialog(null, "Registered: " + username + " (" + email + ")");
                } catch (SQLException ex) {
                    System.out.println(ex);
                    JOptionPane.showMessageDialog(null, "Registration failed!");
                } catch (MessagingException ex) {
                    System.out.println(ex);
                    JOptionPane.showMessageDialog(null, "Failed to send confirmation email.");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        add(panel);
    }

    private void insertData(String username, String email, String password) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/passwordmanager";
        String user = "root";
        String pass = "";

        try (Connection con = DriverManager.getConnection(url, user, pass); PreparedStatement pstmt = con.prepareStatement("INSERT INTO users(username, email, password) VALUES (?, ?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        }
    }

    private void sendEmail(String recipient, String username) throws MessagingException {
        String sender = "suprithvenki@gmail.com";
        String host = "smtp.gmail.com";
        String password = "iftz syqd srcs eocq";
        
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        // Creating the email message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("Registration Successful");
        message.setText("Dear " + username + ",\n\nThank you for registering!\n\nBest Regards,\nYour Team");

        // Sending the email
        Transport.send(message);
        System.out.println("Confirmation email sent successfully to " + recipient);
    }
}
