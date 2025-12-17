package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class GameOverOverlayPanel extends JPanel {
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color PANEL_BG = new Color(18, 18, 18);
    private static final Color SUBTEXT = new Color(170, 170, 170);

    public GameOverOverlayPanel(
            String message,                  //combined game result + reason
            ActionListener onRestart,       //restart game
            ActionListener onQuit           //quit to main menu
    ) {
        setLayout(null);
        setOpaque(false);

        JPanel tint = new JPanel() {//dim background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 185));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        tint.setOpaque(false);
        tint.setLayout(null);
        add(tint);

        JPanel card = new JPanel() {//paneloverlay
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 28;

                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(-6, 8, w + 12, h + 12, r, r);

                g2.setColor(PANEL_BG);
                g2.fillRoundRect(0, 0, w, h, r, r);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(GOLD);
                g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(extractTitle(message), SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(extractReason(message), SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.PLAIN, 15));
        subtitle.setForeground(SUBTEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));

        JButton restartBtn = createPrimaryButton("RESTART");
        JButton quitBtn = createSecondaryButton("QUIT TO MENU");

        buttonRow.add(restartBtn);
        buttonRow.add(Box.createHorizontalStrut(18));
        buttonRow.add(quitBtn);

        card.add(Box.createVerticalStrut(28));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(26));
        card.add(buttonRow);
        card.add(Box.createVerticalStrut(24));

        tint.add(card);

        restartBtn.addActionListener(e -> {//sfx and logic
            AudioManager.playSFX("click");
            onRestart.actionPerformed(e);
        });
        quitBtn.addActionListener(e -> {
            AudioManager.playSFX("click");
            onQuit.actionPerformed(e);
        });
    }

    @Override
    public void doLayout() {
        super.doLayout();

        Component tint = getComponent(0);
        tint.setBounds(0, 0, getWidth(), getHeight());

        JPanel card = (JPanel) ((JPanel) tint).getComponent(0);
        int cardW = 520;
        int cardH = 220;

        card.setBounds((getWidth() - cardW) / 2, (getHeight() - cardH) / 2, cardW, cardH);
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, new Color(222, 184, 63), Color.BLACK);
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, new Color(120, 120, 120), Color.WHITE);
        return btn;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {//styling button
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setMaximumSize(new Dimension(160, 40));
        btn.setFont(new Font("Serif", Font.BOLD, 14));
        btn.setForeground(fg);

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();
                int r = 18;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, r, r);

                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(fg);
                g2.drawString(btn.getText(), (w - fm.stringWidth(btn.getText())) / 2, (h + fm.getAscent()) / 2 - 2);

                g2.dispose();
            }
        });
    }

    private String extractTitle(String msg) {
        int idx = msg.indexOf("(");
        return idx > 0 ? msg.substring(0, idx).trim() : msg;
    }

    private String extractReason(String msg) {
        int idx = msg.indexOf("(");
        return idx > 0 ? msg.substring(idx) : "";
    }
}
