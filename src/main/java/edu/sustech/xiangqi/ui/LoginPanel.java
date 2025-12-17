package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField usernameInput;//core form components
    private JPasswordField passwordInput;
    private JLabel messageLabel;

    private static final int CARD_W = 440;//layout constants
    private static final int CARD_H = 520;
    private static final int CONTENT_W = 280;
    private static final int FIELD_H = 42;

    public LoginPanel(ActionListener onLogin, ActionListener onRegister, ActionListener onGuest) {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

        JPanel content = new JPanel();//main content
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setPreferredSize(new Dimension(CONTENT_W, 360));

        JLabel title = new JLabel("象棋", SwingConstants.CENTER);//title
        title.setFont(new Font("Noto Serif SC", Font.PLAIN, 42)); // Fancy Chinese font
        title.setForeground(new Color(212, 175, 55));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("CHINESE CHESS", SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.PLAIN, 16));
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(" ", SwingConstants.CENTER);//status messages
        messageLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(220, 220, 220));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(12));
        content.add(messageLabel);
        content.add(Box.createVerticalStrut(24));

        usernameInput = new RoundedTextField("Username");//input fields
        passwordInput = new RoundedPasswordField("Password");

        content.add(usernameInput);
        content.add(Box.createVerticalStrut(14));
        content.add(passwordInput);
        content.add(Box.createVerticalStrut(26));

        JButton loginButton = new RoundedButton("LOGIN", new Color(212, 175, 55), Color.BLACK, CONTENT_W, 44);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(loginButton);

        content.add(Box.createVerticalStrut(22));
        JPanel bottom = new JPanel();//register + guest button
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        JButton registerButton = new RoundedButton("Register", new Color(90, 90, 90), Color.WHITE, 130, 36);
        JButton guestButton = new RoundedButton("Guest", new Color(90, 90, 90), Color.WHITE, 130, 36);

        bottom.add(registerButton);
        bottom.add(Box.createHorizontalStrut(18));
        bottom.add(guestButton);
        content.add(bottom);

        add(content);//adds everything to the panel

        loginButton.addActionListener(e -> {//sfx & btn logic
            AudioManager.playSFX("click");
            onLogin.actionPerformed(e);
        });
        registerButton.addActionListener(e -> {
            AudioManager.playSFX("click");
            onRegister.actionPerformed(e);
        });
        guestButton.addActionListener(e -> {
            AudioManager.playSFX("click");
            onGuest.actionPerformed(e);
        });
    }

    public String getUsername() {//analyze inputs
        return usernameInput.getText().trim();
    }
    public String getPassword() {
        return new String(passwordInput.getPassword());
    }
    public void setMessage(String msg) {
        messageLabel.setText(msg);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {//paneloverlay and background
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(10,10,10), 0, h, new Color(5,5,5)));
        g2.fillRect(0, 0, w, h);

        int x = (w - CARD_W) / 2;
        int y = (h - CARD_H) / 2;

        for (int i = 0; i < 20; i++) {//shadow
            g2.setComposite(AlphaComposite.SrcOver.derive(0.04f));
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(x - i, y - i, CARD_W + i*2, CARD_H + i*2, 28, 28);
        }
        g2.setComposite(AlphaComposite.SrcOver);

        g2.setColor(new Color(18,18,18,235));//paneloverlay bg
        g2.fillRoundRect(x, y, CARD_W, CARD_H, 28, 28);

        g2.setStroke(new BasicStroke(1.4f));//border
        g2.setColor(new Color(212,175,55,190));
        g2.drawRoundRect(x, y, CARD_W, CARD_H, 28, 28);

        g2.dispose();
    }

    private static class RoundedTextField extends JTextField {//maketyping field rounded corners
        private final String placeholder;

        RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setMaximumSize(new Dimension(CONTENT_W, FIELD_H));
            setPreferredSize(new Dimension(CONTENT_W, FIELD_H));
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("Serif", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(212,175,55,160));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);

            super.paintComponent(g);

            if (getText().isEmpty()) {
                g2.setColor(new Color(160,160,160));
                g2.drawString(placeholder, 16, getHeight()/2 + 5);
            }
            g2.dispose();
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;

        RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setMaximumSize(new Dimension(CONTENT_W, FIELD_H));
            setPreferredSize(new Dimension(CONTENT_W, FIELD_H));
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("Serif", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(212,175,55,160));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);

            super.paintComponent(g);

            if (getPassword().length == 0) {
                g2.setColor(new Color(160,160,160));
                g2.drawString(placeholder, 16, getHeight()/2 + 5);
            }
            g2.dispose();
        }
    }

    private static class RoundedButton extends JButton {//rounded btn function
        private final Color bg, fg;
        private final int w, h;

        RoundedButton(String text, Color bg, Color fg, int w, int h) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            this.w = w;
            this.h = h;
            setPreferredSize(new Dimension(w, h));
            setMaximumSize(new Dimension(w, h));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Serif", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.BLACK);
            g2.setComposite(AlphaComposite.SrcOver.derive(0.25f));
            g2.fillRoundRect(2, 3, w-4, h-4, 18, 18);

            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w-2, h-2, 18, 18);

            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(
                    getText(),
                    (w - fm.stringWidth(getText())) / 2,
                    (h + fm.getAscent()) / 2 - 2
            );
            g2.dispose();
        }
    }
}
