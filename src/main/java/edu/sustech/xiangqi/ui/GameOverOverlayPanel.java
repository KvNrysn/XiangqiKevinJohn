package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameOverOverlayPanel extends JPanel {

    public GameOverOverlayPanel(
            String resultText,
            ActionListener onRestart,
            ActionListener onQuitToMenu
    ) {
        setLayout(new BorderLayout());

        // === Dim background ===
        JPanel dimPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dimPanel.setLayout(new GridBagLayout());

        // === Center box ===
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // === Title ===
        JLabel title = new JLabel("Game Over");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 28));

        // === Result ===
        JLabel result = new JLabel(resultText);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setFont(new Font("Serif", Font.BOLD, 22));

        if (resultText.contains("Red")) {
            result.setForeground(new Color(180, 0, 0));
        } else if (resultText.contains("Black")) {
            result.setForeground(Color.BLACK);
        } else {
            result.setForeground(Color.DARK_GRAY);
        }

        // === Buttons ===
        JButton restartButton = new JButton("Restart");
        JButton quitButton = new JButton("Quit to Menu");

        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        restartButton.addActionListener(onRestart);
        quitButton.addActionListener(onQuitToMenu);

        // === Layout ===
        box.add(title);
        box.add(Box.createVerticalStrut(15));
        box.add(result);
        box.add(Box.createVerticalStrut(25));
        box.add(restartButton);
        box.add(Box.createVerticalStrut(10));
        box.add(quitButton);

        dimPanel.add(box);
        add(dimPanel, BorderLayout.CENTER);
    }
}
