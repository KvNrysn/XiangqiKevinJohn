package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private JCheckBox musicCheckBox;
    private JSlider volumeSlider;
    private JButton backButton;

    public SettingsPanel(Runnable onBack) {

        setLayout(new GridBagLayout());
        setBackground(new Color(220, 179, 92));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // ===== Title =====
        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // ===== Music Toggle =====
        gbc.gridy++;
        musicCheckBox = new JCheckBox("Background Music");
        musicCheckBox.setOpaque(false);
        musicCheckBox.setFont(new Font("Serif", Font.PLAIN, 16));
        musicCheckBox.setSelected(AudioManager.isEnabled());
        add(musicCheckBox, gbc);

        // ===== Volume Label =====
        gbc.gridy++;
        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setFont(new Font("Serif", Font.PLAIN, 16));
        add(volumeLabel, gbc);

        // ===== Volume Slider =====
        gbc.gridy++;
        volumeSlider = new JSlider(0, 100, (int) (AudioManager.getVolume() * 100));
        volumeSlider.setPreferredSize(new Dimension(220, 40));
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        add(volumeSlider, gbc);

        // ===== Back Button =====
        gbc.gridy++;
        backButton = new JButton("Back to Main Menu");
        add(backButton, gbc);

        // ===== Live Wiring =====
        musicCheckBox.addActionListener(e ->
                AudioManager.setEnabled(musicCheckBox.isSelected())
        );

        volumeSlider.addChangeListener(e ->
                AudioManager.setVolume(volumeSlider.getValue() / 100f)
        );

        backButton.addActionListener(e -> onBack.run());
    }
}
