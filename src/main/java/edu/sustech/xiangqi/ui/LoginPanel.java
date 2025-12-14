package edu.sustech.xiangqi.ui;
import edu.sustech.xiangqi.ui.XiangqiApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class LoginPanel extends JPanel{
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JLabel messageLabel;

    public String getUsername() {
        return usernameInput.getText().trim();
    }

    public String getPassword() {
        return new String(passwordInput.getPassword());
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    public LoginPanel(ActionListener onLogin, ActionListener onRegister, ActionListener onGuest){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);



        JLabel label = new JLabel("象棋");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(new JLabel("象棋", SwingConstants.CENTER), gbc);
        gbc.gridwidth = 1;


        this.messageLabel = new JLabel("CHINESE CHESS");
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        add(messageLabel, gbc);
        messageLabel.setFont(new Font("Noto Serif SC", Font.PLAIN, 24));
        gbc.gridwidth = 1;


        this.usernameInput = new JTextField(15);
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Username:", SwingConstants.CENTER), gbc);
        gbc.gridx = 1;
        add(usernameInput, gbc);


        this.passwordInput = new JPasswordField(15);
        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("Password:", SwingConstants.CENTER), gbc);
        gbc.gridx = 1;
        add(passwordInput, gbc);


        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton guestButton = new JButton("Guest");
        gbc.gridy = 5;
        gbc.gridx = 0;
        add(loginButton, gbc);
        gbc.gridx = 1;
        add(registerButton, gbc);
        gbc.gridx = 2;
        add(guestButton, gbc);

        loginButton.addActionListener(onLogin);
        registerButton.addActionListener(onRegister);
        guestButton.addActionListener(onGuest);
    }
}
