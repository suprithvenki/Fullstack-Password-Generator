package logindemo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PasswordGeneratorGUI extends JFrame {

    private final PasswordGenerator passwordGenerator;
    private final String loggedEmail;

    public PasswordGeneratorGUI(String email) {
        super("Password Generator");
        this.loggedEmail = email;

        setSize(540, 570);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        passwordGenerator = new PasswordGenerator();

        addGuiComponents();
    }

    private void addGuiComponents() {
        JLabel titleLabel = new JLabel("Password Generator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        titleLabel.setBounds(0, 10, 540, 39);
        add(titleLabel);

        JTextArea passwordOutput = new JTextArea();
        passwordOutput.setEditable(false);
        passwordOutput.setFont(new Font("Dialog", Font.BOLD, 22));
        JScrollPane passwordOutputPane = new JScrollPane(passwordOutput);
        passwordOutputPane.setBounds(25, 97, 479, 70);
        passwordOutputPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(passwordOutputPane);

        JLabel passwordLengthLabel = new JLabel("Password Length: ");
        passwordLengthLabel.setFont(new Font("Dialog", Font.PLAIN, 22));
        passwordLengthLabel.setBounds(25, 215, 272, 39);
        add(passwordLengthLabel);

        JTextArea passwordLengthInputArea = new JTextArea();
        passwordLengthInputArea.setFont(new Font("Dialog", Font.PLAIN, 32));
        passwordLengthInputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordLengthInputArea.setBounds(310, 215, 192, 39);
        add(passwordLengthInputArea);

        JToggleButton uppercaseToggle = new JToggleButton("Uppercase");
        uppercaseToggle.setFont(new Font("Dialog", Font.PLAIN, 21));
        uppercaseToggle.setBounds(25, 302, 225, 56);
        add(uppercaseToggle);

        JToggleButton lowercaseToggle = new JToggleButton("Lowercase");
        lowercaseToggle.setFont(new Font("Dialog", Font.PLAIN, 21));
        lowercaseToggle.setBounds(282, 302, 225, 56);
        add(lowercaseToggle);

        JToggleButton numbersToggle = new JToggleButton("Numbers");
        numbersToggle.setFont(new Font("Dialog", Font.PLAIN, 21));
        numbersToggle.setBounds(25, 373, 225, 56);
        add(numbersToggle);

        JToggleButton symbolsToggle = new JToggleButton("Symbols");
        symbolsToggle.setFont(new Font("Dialog", Font.PLAIN, 21));
        symbolsToggle.setBounds(282, 373, 225, 56);
        add(symbolsToggle);

        JButton generateButton = new JButton("Generate");
        generateButton.setFont(new Font("Dialog", Font.PLAIN, 23));
        generateButton.setBounds(155, 477, 222, 41);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int passwordLength = Integer.parseInt(passwordLengthInputArea.getText());
                    boolean anyToggleSelected = lowercaseToggle.isSelected() || uppercaseToggle.isSelected() || numbersToggle.isSelected() || symbolsToggle.isSelected();

                    if (passwordLength <= 0 || !anyToggleSelected) {
                        passwordOutput.setText("Please select a valid length and at least one option.");
                        return;
                    }

                    String generatedPassword = passwordGenerator.generatePassword(
                            passwordLength,
                            uppercaseToggle.isSelected(),
                            lowercaseToggle.isSelected(),
                            numbersToggle.isSelected(),
                            symbolsToggle.isSelected()
                    );

                    passwordOutput.setText(generatedPassword);
                    savePasswordToDatabase(generatedPassword, loggedEmail);
                    sendEmail(loggedEmail, generatedPassword);
                    
                    Options option = new Options(loggedEmail);  
                        option.setVisible(true);
                        dispose();

                } catch (NumberFormatException ex) {
                    passwordOutput.setText("Enter a valid number for password length.");
                } catch (MessagingException ex) {
                    passwordOutput.setText("Failed to send email: " + ex.getMessage());
                }
            }
        });
        add(generateButton);
    }

    private void savePasswordToDatabase(String password, String email) {
        String url = "jdbc:mysql://localhost:3306/passwordmanager";
        String user = "root";
        String pass = "";

        // Check if the password already exists for the given email
        String selectQuery = "SELECT * FROM passwords WHERE email = ?";
        String insertQuery = "INSERT INTO passwords (email, password) VALUES (?, ?)";
        String updateQuery = "UPDATE passwords SET password = ? WHERE email = ?";

        try (Connection con = DriverManager.getConnection(url, user, pass); PreparedStatement selectStmt = con.prepareStatement(selectQuery)) {

            selectStmt.setString(1, email);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Email exists, so we update the password
                try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, password);
                    updateStmt.setString(2, email);
                    updateStmt.executeUpdate();
                    System.out.println("Password updated in database.");
                }
            } else {
                // Email does not exist, so we insert a new password
                try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, email);
                    insertStmt.setString(2, password);
                    insertStmt.executeUpdate();
                    System.out.println("Password saved to database.");
                }
            }
        } catch (SQLException ex) {
            System.out.println("Failed to save or update password in database.");
        }
    }

    private void sendEmail(String recipient, String password) throws MessagingException {
        String sender = "suprithvenki@gmail.com";
        String host = "smtp.gmail.com";
        String senderPassword = "iftz syqd srcs eocq";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, senderPassword);
            }
        });

        // Creating the email message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("Password sent Successful");
        message.setText("Your Generated Password is " + password);

        // Sending the email
        Transport.send(message);
        System.out.println("Confirmation email sent successfully to " + recipient);
    }

}
