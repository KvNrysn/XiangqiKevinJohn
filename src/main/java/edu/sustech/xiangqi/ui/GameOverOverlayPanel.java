package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameOverOverlayPanel extends JPanel {

    public GameOverOverlayPanel(
            String message,
            ActionListener onRestart,
            ActionListener onQuit
    ) {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== Dim background =====
        JPanel dimPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimPanel.setOpaque(false);
        dimPanel.setLayout(new GridBagLayout());

        // ===== Center box =====
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 20, 14, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // ===== Title =====
        JLabel title = new JLabel(message, SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        box.add(title, gbc);

        // ===== Restart =====
        gbc.gridy++;
        JButton restartButton = new JButton("Restart");
        box.add(restartButton, gbc);

        // ===== Quit =====
        gbc.gridy++;
        JButton quitButton = new JButton("Quit to Menu");
        box.add(quitButton, gbc);

        restartButton.addActionListener(onRestart);
        quitButton.addActionListener(onQuit);

        dimPanel.add(box);
        add(dimPanel, BorderLayout.CENTER);
    }
}
