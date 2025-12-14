package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {

    private JButton startButton;
    private JButton continueButton;
    private JButton settingsButton;
    private JButton exitButton;

    public MainMenuPanel(
            boolean isGuest,
            boolean hasSavedGame,
            ActionListener onStart,
            ActionListener onContinue,
            ActionListener onSettings,
            ActionListener onExit) {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Noto Serif SC", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);
        gbc.gridwidth = 1;

        // Start Game button
        startButton = new JButton("Start Game");
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(startButton, gbc);

        // Continue button
        continueButton = new JButton("Continue");
        gbc.gridy = 2;
        add(continueButton, gbc);

        // Disable if guest OR no save
        if (isGuest || !hasSavedGame) {
            continueButton.setEnabled(false);
        }

        // Settings button
        settingsButton = new JButton("Settings");
        gbc.gridy = 3;
        add(settingsButton, gbc);

        // Exit button
        exitButton = new JButton("Exit");
        gbc.gridy = 4;
        add(exitButton, gbc);

        // Hook up logic
        startButton.addActionListener(onStart);
        continueButton.addActionListener(onContinue);
        settingsButton.addActionListener(onSettings);
        exitButton.addActionListener(onExit);
    }
}
