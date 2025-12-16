package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PauseOverlayPanel extends JPanel {

    private static final int CARD_W = 360;
    private static final int CARD_H = 420;

    private static final int BTN_W_PRIMARY = 260;
    private static final int BTN_W_SECONDARY = 230;
    private static final int BTN_H = 40;

    private static final Color GOLD = new Color(212, 175, 55);

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

        // ===================== FULL-SCREEN DIM =====================
        JPanel dimPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, w, h);

                int cx = (w - CARD_W) / 2;
                int cy = (h - CARD_H) / 2;

                for (int i = 0; i < 80; i++) {
                    g2.setColor(new Color(0, 0, 0, Math.max(0, 20 - i / 2)));
                    g2.fillRoundRect(
                            cx - i,
                            cy - i,
                            CARD_W + i * 2,
                            CARD_H + i * 2,
                            28 + i,
                            28 + i
                    );
                }

                g2.dispose();
            }
        };
        dimPanel.setOpaque(false);
        dimPanel.setLayout(new GridBagLayout());

        // ===================== CARD =====================
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(18, 18, 18));
                g2.fillRoundRect(0, 0, w, h, 24, 24);

                g2.setStroke(new BasicStroke(1.4f));
                g2.setColor(GOLD);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // ===================== CONTENT =====================
        card.add(Box.createVerticalGlue());

        JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.PLAIN, 22));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(28));

        JButton resumeBtn  = createButton("RESUME", true, BTN_W_PRIMARY);
        JButton saveBtn    = createButton("SAVE GAME", false, BTN_W_SECONDARY);
        JButton restartBtn = createButton("RESTART", false, BTN_W_SECONDARY);
        JButton resignBtn  = createButton(isRedTurn ? "RED RESIGN" : "BLACK RESIGN", false, BTN_W_SECONDARY);
        JButton quitBtn    = createButton("QUIT TO MENU", false, BTN_W_SECONDARY);

        // ===== Guest logic =====
        if (isGuest) {
            saveBtn.setEnabled(false);
            saveBtn.setCursor(Cursor.getDefaultCursor());
            saveBtn.setToolTipText("Guests cannot save games");
        }

        card.add(resumeBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(restartBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(resignBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(quitBtn);

        card.add(Box.createVerticalGlue());

        // ===================== ACTIONS =====================
        resumeBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onResume.actionPerformed(e);
        });

        saveBtn.addActionListener(e -> {
            if (!saveBtn.isEnabled()) return;
            AudioManager.playSFX("click");
            onSave.actionPerformed(e);
        });

        restartBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onRestart.actionPerformed(e);
        });

        resignBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onResign.actionPerformed(e);
        });

        quitBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onQuit.actionPerformed(e);
        });

        dimPanel.add(card);
        add(dimPanel, BorderLayout.CENTER);
    }

    // ===================== BUTTON =====================
    private JButton createButton(String text, boolean primary, int width) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                float alpha = isEnabled() ? 1.0f : 0.45f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                int w = getWidth();
                int h = getHeight();
                int r = 18;

                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(3, 4, w - 6, h - 6, r, r);

                g2.setColor(primary ? GOLD : new Color(90, 90, 90));
                g2.fillRoundRect(0, 0, w - 2, h - 2, r, r);

                g2.setColor(primary ? Color.BLACK : Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(
                        getText(),
                        (w - fm.stringWidth(getText())) / 2,
                        (h + fm.getAscent()) / 2 - 2
                );

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(width, BTN_H));
        btn.setMaximumSize(new Dimension(width, BTN_H));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Serif", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }
}
