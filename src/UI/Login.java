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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

public class UI extends JFrame {
    
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton loginButton;
    private Jlaberl messageLabel;

    public UI(){
        super();
        init();
    }

    public void init(){
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        messageLabel = new JLabel();

        usernameField.setSize(200, 30);
        passwordField.setSize(200, 30);
        loginButton.setSize(100, 30);

        setPosition(usernameLabel, 50, 50);
        setPosition(usernameField, 150, 50);
        setPosition(passwordLabel, 50, 100);
        setPosition(passwordField, 150, 100);
        setPosition(loginButton, 150, 150);
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if(login.authenticate(username, password)){
                // Authentication successful
                System.out.println("Login successful!");
                
            } else {
                // Authentication failed
                System.out.println("Invalid username or password.");
                messageLabel.setText("Invalid username or password.");
            }

        });
    }

    
}
