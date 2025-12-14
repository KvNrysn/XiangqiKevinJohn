package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.io.*;
import java.awt.*;

import edu.sustech.xiangqi.audio.AudioManager;
import edu.sustech.xiangqi.model.ChessBoardModel;


public class XiangqiApplication {

    private static final File USER_FILE = new File("data/users.txt");
    private static SettingsPanel settingsPanel;
    private static String currentUsername = null;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            AudioManager.init();
            AudioManager.play();

            new File("data/saves").mkdirs(); // ✅ REQUIRED

            JFrame frame = new JFrame("中国象棋");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);

            final LoginPanel[] loginPanel = new LoginPanel[1];

            loginPanel[0] = new LoginPanel(
                    e -> {
                        String u = loginPanel[0].getUsername();
                        String p = loginPanel[0].getPassword();

                        if (u.isEmpty() || p.isEmpty()) {
                            loginPanel[0].setMessage("Username and password cannot be empty.");
                            return;
                        }

                        if (checkCredentials(u, p)) {
                            currentUsername = u;
                            switchToMainMenu(frame, false, checkSaveExists(u));
                        } else {
                            loginPanel[0].setMessage("Invalid username or password.");
                        }
                    },
                    e -> {
                        String u = loginPanel[0].getUsername();
                        String p = loginPanel[0].getPassword();

                        if (userExists(u)) {
                            loginPanel[0].setMessage("User already exists.");
                        } else {
                            saveUser(u, p);
                            loginPanel[0].setMessage("Registration successful!");
                        }
                    },
                    e -> {
                        currentUsername = null;
                        switchToMainMenu(frame, true, false);
                    }
            );

            frame.setContentPane(loginPanel[0]);
            frame.setVisible(true);
        });
    }

    // ===================== NAVIGATION =====================

    private static void switchToGame(JFrame frame, boolean isGuest, boolean loadSave) {
        ChessBoardModel model = new ChessBoardModel();

        File saveFile = null;
        if (!isGuest && currentUsername != null) {
            saveFile = new File("data/saves/" + currentUsername + ".save");
        }

        // === CONTINUE GAME ===
        if (!isGuest && loadSave && saveFile != null && saveFile.exists()) {
            model.loadGame(saveFile.getPath());
        }

        // === NEW GAME: DELETE OLD SAVE ===
        if (!isGuest && !loadSave && saveFile != null && saveFile.exists()) {
            saveFile.delete();
        }

        ChessBoardPanel gamePanel = new ChessBoardPanel(
                model,
                isGuest,
                currentUsername,
                () -> switchToGame(frame, isGuest, false),   // restart
                () -> switchToMainMenu(frame, isGuest, false)
        );

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(220, 179, 92));
        wrapper.add(gamePanel);

        frame.setContentPane(wrapper);
        frame.revalidate();
        frame.repaint();
    }


    private static void switchToMainMenu(JFrame frame, boolean isGuest, boolean hasSave) {
        MainMenuPanel menu = new MainMenuPanel(
                isGuest,
                hasSave,
                e -> switchToGame(frame, isGuest, false), // new game
                e -> switchToGame(frame, isGuest, true),  // continue
                e -> switchToSettings(frame),
                e -> System.exit(0)
        );

        frame.setContentPane(menu);
        frame.revalidate();
        frame.repaint();
    }

    // ===================== SAVE CHECK =====================

    private static boolean checkSaveExists(String username) {
        return new File("data/saves/" + username + ".save").exists();
    }

    // ===================== USER FILE =====================

    private static boolean checkCredentials(String u, String p) {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(":");
                if (s[0].equals(u) && s[1].equals(p)) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static boolean userExists(String u) {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null)
                if (line.startsWith(u + ":")) return true;
        } catch (IOException ignored) {}
        return false;
    }

    private static void saveUser(String u, String p) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(u + ":" + p);
            bw.newLine();
        } catch (IOException ignored) {}
    }

    private static void switchToSettings(JFrame frame) {
        settingsPanel = new SettingsPanel(
                () -> switchToMainMenu(frame, false, false)
        );
        frame.setContentPane(settingsPanel);
        frame.revalidate();
    }
}
