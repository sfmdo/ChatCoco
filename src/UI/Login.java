/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author sfmdo
 */



import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Component;

public class Login extends JFrame {

    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    public Login() {
        super();
        init();
    }

    public void init() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");
        messageLabel = new JLabel("");

        setPosition(usernameLabel, 50, 50, 100, 30);
        setPosition(usernameField, 150, 50, 180, 30);

        setPosition(passwordLabel, 50, 100, 100, 30);
        setPosition(passwordField, 150, 100, 180, 30);

        setPosition(loginButton, 150, 150, 100, 30);
        setPosition(messageLabel, 100, 200, 250, 30);

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(messageLabel);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (authenticate(username, password)) {
                System.out.println("Login successful!");

                Content content = new Content();
                content.setVisible(true);

                dispose();
            } else {
                System.out.println("Invalid username or password.");
                messageLabel.setText("Invalid username or password.");
            }
        });
    }

    private void setPosition(Component component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
    }

    private boolean authenticate(String username, String password) {
        return username.equals("pepe") && password.equals("6767");
    }
}