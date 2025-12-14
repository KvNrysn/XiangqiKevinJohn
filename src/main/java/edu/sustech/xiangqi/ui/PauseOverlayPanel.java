package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PauseOverlayPanel extends JPanel {

    private JButton resumeButton;
    private JButton saveButton;
    private JButton restartButton;
    private JButton resignButton;
    private JButton quitButton;

    public PauseOverlayPanel(
            boolean isGuest,
            boolean isRedTurn,
            ActionListener onResume,
            ActionListener onSave,
            ActionListener onRestart,
            ActionListener onResign,
            ActionListener onQuit
    ) {

        setLayout(new BorderLayout());
        setOpaque(false);

        // === Dim background ===
        JPanel dimPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimPanel.setOpaque(false);
        dimPanel.setLayout(new GridBagLayout());

        // === Center box ===
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // === Title ===
        JLabel title = new JLabel("Paused", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        box.add(title, gbc);

        // === Resume ===
        gbc.gridy++;
        resumeButton = new JButton("Resume");
        box.add(resumeButton, gbc);

        // === Save ===
        gbc.gridy++;
        saveButton = new JButton("Save Game");
        saveButton.setEnabled(!isGuest);
        box.add(saveButton, gbc);

        // === Restart ===
        gbc.gridy++;
        restartButton = new JButton("Restart");
        box.add(restartButton, gbc);

        // === Resign ===
        gbc.gridy++;
        resignButton = new JButton(isRedTurn ? "Red resign" : "Black resign");
        box.add(resignButton, gbc);

        // === Quit ===
        gbc.gridy++;
        quitButton = new JButton("Quit to Menu");
        box.add(quitButton, gbc);

        // === Wire actions ===
        resumeButton.addActionListener(onResume);
        saveButton.addActionListener(onSave);
        restartButton.addActionListener(onRestart);
        resignButton.addActionListener(onResign);
        quitButton.addActionListener(onQuit);

        dimPanel.add(box);
        add(dimPanel, BorderLayout.CENTER);
    }
}
