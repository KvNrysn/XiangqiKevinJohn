package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {
    private static final int CARD_WIDTH = 380;//layout constants
    private static final int CARD_HEIGHT = 460;

    private static final int BTN_W_PRIMARY = 270;
    private static final int BTN_W_SECONDARY = 235;

    private static final int BTN_H_PRIMARY = 46;
    private static final int BTN_H_SECONDARY = 42;

    private static final int CARD_RADIUS = 18;
    private static final Color GOLD = new Color(210, 175, 80);

    public MainMenuPanel(boolean isGuest, boolean hasSavedGame, ActionListener onStart, ActionListener onContinue, ActionListener onSettings, ActionListener onExit) {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.BLACK);//black bg

        JPanel card = new CardPanel();//main paneloverlay container (centered panel)
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 0;//title
        gbc.insets = new Insets(28, 0, 36, 0);

        JLabel title = new JLabel("MAIN MENU", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.PLAIN, 22));
        title.setForeground(new Color(220, 190, 110));
        card.add(title, gbc);

        gbc.gridy++;//start game
        gbc.insets = new Insets(0, 0, 26, 0);

        JButton startBtn = new ShadowButton("START GAME", true);
        startBtn.setPreferredSize(new Dimension(BTN_W_PRIMARY, BTN_H_PRIMARY));
        card.add(startBtn, gbc);

        gbc.gridy++;//continue
        gbc.insets = new Insets(0, 0, 18, 0);

        JButton continueBtn = new ShadowButton("CONTINUE", false);
        continueBtn.setPreferredSize(new Dimension(BTN_W_SECONDARY, BTN_H_SECONDARY));
        continueBtn.setEnabled(!isGuest && hasSavedGame);  // Only enable if applicable
        card.add(continueBtn, gbc);

        gbc.gridy++;//settings
        gbc.insets = new Insets(0, 0, 18, 0);

        JButton settingsBtn = new ShadowButton("SETTINGS", false);
        settingsBtn.setPreferredSize(new Dimension(BTN_W_SECONDARY, BTN_H_SECONDARY));
        card.add(settingsBtn, gbc);

        gbc.gridy++;//exit
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton exitBtn = new ShadowButton("EXIT", false);
        exitBtn.setPreferredSize(new Dimension(BTN_W_SECONDARY, BTN_H_SECONDARY));
        card.add(exitBtn, gbc);

        startBtn.addActionListener(e -> {//sfx and button logic
            AudioManager.playSFX("click");
            onStart.actionPerformed(e);
        });
        continueBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onContinue.actionPerformed(e);
        });
        settingsBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onSettings.actionPerformed(e);
        });
        exitBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onExit.actionPerformed(e);
        });
        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, getWidth(), getHeight(), new Color(18, 16, 14));

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private static class CardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            for (int i = 0; i < 18; i++) {//outer glow (soft dark blur)
                g2.setColor(new Color(0, 0, 0, Math.max(0, 26 - i)));
                g2.fillRoundRect(14 - i, 14 - i, w - 28 + i * 2, h - 28 + i * 2, CARD_RADIUS + i, CARD_RADIUS + i);
            }

            g2.setColor(new Color(18, 18, 18));//bg fill
            g2.fillRoundRect(14, 14, w - 28, h - 28, CARD_RADIUS, CARD_RADIUS);

            g2.setStroke(new BasicStroke(1.4f));//gold border
            g2.setColor(GOLD);
            g2.drawRoundRect(14, 14, w - 28, h - 28, CARD_RADIUS, CARD_RADIUS);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ShadowButton extends JButton {//btn shadow
        private final boolean primary;
        private final int radius;

        ShadowButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            this.radius = primary ? 22 : 20;

            setFont(new Font("Serif", Font.PLAIN, primary ? 15 : 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(primary ? Color.BLACK : Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean enabled = isEnabled();

            for (int i = 0; i < 10; i++) {//drop shadow
                g2.setColor(new Color(0, 0, 0, Math.max(0, 18 - i * 2)));
                g2.fillRoundRect(3, 4 + i / 2, w - 6, h - 6, radius, radius);
            }

            Color fillColor = primary ? (enabled ? new Color(210, 175, 80) : new Color(210, 175, 80, 140)) : (enabled ? new Color(95, 95, 95) : new Color(95, 95, 95, 120));

            g2.setColor(fillColor);
            g2.fillRoundRect(3, 3, w - 6, h - 6, radius, radius);

            Color fg = getForeground();
            if (!enabled) {
                fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 150);
            }
            g2.setColor(fg);

            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }
}
