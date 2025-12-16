package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SettingsPanel extends JPanel {

    private JCheckBox musicCheckBox;
    private JSlider volumeSlider;
    private JButton backButton;

    // Palette
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color CARD_BG_TOP = new Color(20, 20, 20);
    private static final Color CARD_BG_BOT = new Color(10, 10, 10);

    public SettingsPanel(Runnable onBack) {

        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

        // ===== Card container (layout) =====
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Soft inner gradient
                g2.setPaint(new GradientPaint(0, 0, CARD_BG_TOP, 0, h, CARD_BG_BOT));
                g2.fillRoundRect(0, 0, w, h, 28, 28);

                // Gold thin border
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 28, 28);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(44, 56, 44, 56));
        card.setPreferredSize(new Dimension(560, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.CENTER;

        // ===== Title =====
        JLabel title = new JLabel("SETTINGS");
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setForeground(GOLD);
        card.add(title, c);

        // ===== Checkbox row =====
        c.gridy++;
        musicCheckBox = new JCheckBox("Background Music");
        musicCheckBox.setOpaque(false);
        musicCheckBox.setForeground(new Color(235, 235, 235));
        musicCheckBox.setFont(new Font("Serif", Font.PLAIN, 16));
        musicCheckBox.setSelected(AudioManager.isMusicEnabled());

        // Keep checkbox styled (black inside, gold border, gold check)
        musicCheckBox.setIcon(new GoldCheckboxIcon(false));
        musicCheckBox.setSelectedIcon(new GoldCheckboxIcon(true));
        musicCheckBox.setFocusPainted(false);
        musicCheckBox.setBorderPainted(false);
        musicCheckBox.setContentAreaFilled(false);

        card.add(musicCheckBox, c);

        // ===== Volume label =====
        c.gridy++;
        JLabel volumeLabel = new JLabel("Music Volume");
        volumeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        volumeLabel.setForeground(new Color(230, 230, 230));
        card.add(volumeLabel, c);

        // ===== Slider =====
        c.gridy++;
        volumeSlider = new JSlider(0, 100, Math.round(AudioManager.getMusicVolume() * 100f));
        volumeSlider.setOpaque(false);
        volumeSlider.setPreferredSize(new Dimension(420, 70));
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setForeground(GOLD);     // tick + label color
        volumeSlider.setFocusable(true);      // focus can exist, but we DO NOT draw it
        volumeSlider.setFocusTraversalKeysEnabled(false);

        // Custom gold slider UI that also removes the focus rectangle
        volumeSlider.setUI(new GoldSliderUI(volumeSlider));

        card.add(volumeSlider, c);

        // ===== Back button =====
        c.gridy++;
        c.insets = new Insets(28, 10, 10, 10);

        backButton = new RoundedButton("BACK TO MAIN MENU", false);
        backButton.setPreferredSize(new Dimension(300, 52));
        card.add(backButton, c);

        // Add card to center
        add(card, new GridBagConstraints());

        // ===== Wiring =====
        musicCheckBox.addActionListener(e ->
                AudioManager.setMusicEnabled(musicCheckBox.isSelected())
        );

        volumeSlider.addChangeListener(e ->
                AudioManager.setMusicVolume(volumeSlider.getValue() / 100f)
        );

        backButton.addActionListener(e -> {
            AudioManager.playSFX("click");
            onBack.run();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient (subtle)
        g2.setPaint(new GradientPaint(
                0, 0, new Color(12, 12, 12),
                0, getHeight(), Color.BLACK
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    // =========================================================
    // Custom slider UI (gold track + gold thumb) + NO focus ring
    // =========================================================
    private static class GoldSliderUI extends BasicSliderUI {
        private final Color trackBack = new Color(70, 70, 70);
        private final Color trackGold = GOLD;

        GoldSliderUI(JSlider b) {
            super(b);
        }

        @Override
        public void paintFocus(Graphics g) {
            // This is the ACTUAL fix: do not paint the blue focus rectangle.
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = trackRect;
            int cy = r.y + r.height / 2;
            int left = r.x;
            int right = r.x + r.width;

            int thickness = 6;
            int half = thickness / 2;

            // Background track
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(trackBack);
            g2.drawLine(left, cy, right, cy);

            // Filled (gold) part until thumb
            int thumbX = thumbRect.x + thumbRect.width / 2;
            g2.setColor(trackGold);
            g2.drawLine(left, cy, thumbX, cy);

            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = thumbRect.x + thumbRect.width / 2;
            int cy = thumbRect.y + thumbRect.height / 2;

            int w = 16;
            int h = 22;

            Shape thumb = new RoundRectangle2D.Float(cx - w / 2f, cy - h / 2f, w, h, 12, 12);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 110));
            g2.translate(0, 2);
            g2.fill(thumb);
            g2.translate(0, -2);

            // Thumb fill
            g2.setColor(new Color(240, 230, 200));
            g2.fill(thumb);

            // Gold border
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(thumb);

            g2.dispose();
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(20, 28);
        }
    }

    // =========================================================
    // Checkbox icon: black fill + gold border, gold check when on
    // =========================================================
    private static class GoldCheckboxIcon implements Icon {
        private final boolean checked;

        GoldCheckboxIcon(boolean checked) {
            this.checked = checked;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int s = getIconWidth();
            int arc = 6;

            // Inner fill (black)
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(x, y, s, s, arc, arc);

            // Gold border
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x + 1, y + 1, s - 3, s - 3, arc, arc);

            if (checked) {
                // Gold checkmark
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + s / 2, x + s / 2 - 1, y + s - 5);
                g2.drawLine(x + s / 2 - 1, y + s - 5, x + s - 4, y + 5);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    // =========================================================
    // Rounded button (same look as your other screens)
    // =========================================================
    private static class RoundedButton extends JButton {
        private final boolean primary;

        RoundedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setFont(new Font("Serif", Font.PLAIN, 18));
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 22;

            // Shadow (soft)
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc);

            // Button fill
            if (primary) {
                g2.setColor(GOLD);
            } else {
                g2.setColor(new Color(95, 95, 95));
            }
            g2.fillRoundRect(2, 2, w - 4, h - 6, arc, arc);

            // Text
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - 2;

            g2.setColor(primary ? Color.BLACK : Color.WHITE);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }
}