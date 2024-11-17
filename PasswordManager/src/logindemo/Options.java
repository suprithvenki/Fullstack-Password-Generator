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

public class Options extends JFrame {

    private final String loggedEmail;

    public Options(String email) {
        super("Select Options");
        this.loggedEmail = email;

        setSize(540, 570);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addToGUI();
    }

    private void addToGUI() {
        JLabel titleLabel = new JLabel("Select Any Options", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        titleLabel.setBounds(0, 10, 540, 39);
        add(titleLabel);

        JButton newPassword = new JButton("Create New");
        newPassword.setFont(new Font("Dialog", Font.PLAIN, 23));
        newPassword.setBounds(25, 100, 225, 56);
        add(newPassword);

        JButton oldPassword = new JButton("Update Password");
        oldPassword.setFont(new Font("Dialog", Font.PLAIN, 23));
        oldPassword.setBounds(25, 180, 225, 56);
        add(oldPassword);

        JButton removePassword = new JButton("Remove password");
        removePassword.setFont(new Font("Dialog", Font.PLAIN, 23));
        removePassword.setBounds(25, 260, 225, 56);
        add(removePassword);

        newPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasExistingPassword()) {
                    JOptionPane.showMessageDialog(null, "A password already exists for this email.Select update ");
                } else {
                    PasswordGeneratorGUI passwordGeneratorGUI = new PasswordGeneratorGUI(loggedEmail);
                    passwordGeneratorGUI.setVisible(true);
                    dispose();
                }
            }
        });

        oldPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasExistingPassword()) {
                    PasswordGeneratorGUI passwordGeneratorGUI = new PasswordGeneratorGUI(loggedEmail);
                    passwordGeneratorGUI.setVisible(true);
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(null, "No password has been registered from this email");

                }
            }
        });

        removePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasExistingPassword()) {
                    deletePassword();
                    JOptionPane.showMessageDialog(null, "Password for " + loggedEmail + " deleted successfully");
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(null, "No password has been registered from this email");

                }
            }
        });
    }

    private boolean hasExistingPassword() {
        String url = "jdbc:mysql://localhost:3306/passwordmanager";
        String user = "root";
        String pass = "";
        String query = "SELECT * FROM passwords WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, pass); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedEmail);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error checking existing password.");
            return false;
        }
    }

    private boolean deletePassword() {
        String url = "jdbc:mysql://localhost:3306/passwordmanager";
        String user = "root";
        String pass = "";
        String query = "delete from passwords WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, pass); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, loggedEmail);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error checking existing password.");
            return false;
        }
    }

}
