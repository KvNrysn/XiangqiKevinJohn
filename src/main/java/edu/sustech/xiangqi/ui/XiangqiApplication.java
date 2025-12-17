package edu.sustech.xiangqi.ui;

import javax.swing.*;
import java.io.*;
import java.awt.*;

import edu.sustech.xiangqi.audio.AudioManager;
import edu.sustech.xiangqi.model.ChessBoardModel;

public class XiangqiApplication {
    private static final File USER_FILE = new File("data/users.txt"); //txt file with logind ata
    private static String currentUsername = null;//just for UX purposes

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { //schedule GUI init
            AudioManager.init();//start bgm
            new File("data").mkdirs();//create new folder if user account
            new File("data/saves").mkdirs();
            ensureUserFileExists();//making sure that account file exists

            JFrame mainWindow = new JFrame("中国象棋");
            mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainWindow.setSize(900, 700);
            mainWindow.setResizable(false);//window have locked aspect ratio
            mainWindow.setLocationRelativeTo(null);//window should at the center

            final LoginPanel[] loginPanelHolder = new LoginPanel[1];//using array to allow lambda function access login panel

            loginPanelHolder[0] = new LoginPanel(
                    e -> {//login button clicked
                        String usernameInput = loginPanelHolder[0].getUsername();
                        String passwordInput = loginPanelHolder[0].getPassword();

                        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                            loginPanelHolder[0].setMessage("Username and password cannot be empty.");
                            return;
                        }
                        if (checkCredentials(usernameInput, passwordInput)) {
                            currentUsername = usernameInput;
                            boolean hasSave = checkSaveExists(usernameInput);
                            switchToMainMenu(mainWindow, false, hasSave);//if false not a guest
                        } else {
                            loginPanelHolder[0].setMessage("Invalid username or password.");
                        }
                    },

                    e -> {//register button clicked
                        String usernameInput = loginPanelHolder[0].getUsername();
                        String passwordInput = loginPanelHolder[0].getPassword();

                        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                            loginPanelHolder[0].setMessage("Username and password cannot be empty.");
                            return;
                        }
                        if (userExists(usernameInput)) {
                            loginPanelHolder[0].setMessage("User already exists.");
                        } else {
                            saveUser(usernameInput, passwordInput);
                            loginPanelHolder[0].setMessage("Registration successful!");
                        }
                    },

                    e -> {//guest button clicked
                        currentUsername = null;
                        switchToMainMenu(mainWindow, true, false);
                    }
            );
            mainWindow.setContentPane(loginPanelHolder[0]);//display login panel
            mainWindow.setVisible(true);
        });
    }


    private static void switchToGame(JFrame frame, boolean isGuest, boolean loadSave) {//change to main game screen
        ChessBoardModel model = new ChessBoardModel();
        File saveFile = null;

        if (!isGuest && currentUsername != null) {//set up save file path if not guest
            saveFile = new File("data/saves/" + currentUsername + ".save");
        }
        if (!isGuest && loadSave && saveFile != null && saveFile.exists()) {//load saved game if continue button pressed and file exist
            model.loadGame(saveFile.getPath());
        }
        if (!isGuest && !loadSave && saveFile != null && saveFile.exists()) {//delete old save if new game started
            saveFile.delete();
        }

        ChessBoardPanel gamePanel = new ChessBoardPanel(//set up game panel UI
                model,
                isGuest,
                currentUsername,
                () -> switchToGame(frame, isGuest, false),//restart button
                () -> switchToMainMenu(
                        frame,
                        isGuest,
                        !isGuest && currentUsername != null && checkSaveExists(currentUsername)
                )
        );

        JPanel centerWrapper = new JPanel(new GridBagLayout());//put the game board in the center
        centerWrapper.setBackground(new Color(220, 179, 92));
        centerWrapper.add(gamePanel);
        frame.setContentPane(centerWrapper);
        frame.revalidate();
        frame.repaint();
    }


    private static void switchToMainMenu(JFrame frame, boolean isGuest, boolean hasSave) {//mainmenupanel
        MainMenuPanel menu = new MainMenuPanel(
                isGuest,
                hasSave,
                e -> switchToGame(frame, isGuest, false),//new Game
                e -> switchToGame(frame, isGuest, true),//continue Game
                e -> switchToSettings(frame),//settings
                e -> System.exit(0)//exit
        );
        frame.setContentPane(menu);
        frame.revalidate();
        frame.repaint();
    }


    private static void switchToSettings(JFrame frame) {//settings panel
        SettingsPanel settingsPanel = new SettingsPanel(
                () -> switchToMainMenu(
                        frame,
                        false,
                        currentUsername != null && checkSaveExists(currentUsername)
                )
        );
        frame.setContentPane(settingsPanel);
        frame.revalidate();
    }


    private static boolean checkSaveExists(String username) {//check if user data file exists
        if (username == null) return false;
        return new File("data/saves/" + username + ".save").exists();
    }


    private static void ensureUserFileExists() {//generate userdata file
        try {
            File parentDir = USER_FILE.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();//create 'data' folder if missing
            }
            if (!USER_FILE.exists()) {
                USER_FILE.createNewFile();//create empty txt file
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize user file: " + USER_FILE.getPath());//if file creation fails log error
            e.printStackTrace();
        }
    }


    private static boolean checkCredentials(String username, String password) {//compare & check data with input
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userParts = line.split(":");
                if (userParts.length >= 2 && userParts[0].equals(username) && userParts[1].equals(password)) {//check if username and password match
                    return true;
                }
            }
        } catch (IOException ignored) {
            //silently ignore file read issues
        }
        return false;
    }


    private static boolean userExists(String username) {//check if user already exist
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username + ":")) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }


    private static void saveUser(String username, String password) {//save account name and password input
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + ":" + password);
            writer.newLine();//add newline between entries
        } catch (IOException ignored) {
            //empty log bcs not needed
        }
    }
}
