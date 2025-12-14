package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.model.AbstractPiece;
import edu.sustech.xiangqi.model.ChessBoardModel;
import edu.sustech.xiangqi.model.SoldierPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.io.File;


public class ChessBoardPanel extends JPanel {

    // ===== Turn lock / animation =====
    private boolean turnLocked = false;
    private Timer turnLockTimer;
    private Timer turnAnimTimer;


    // ===== Feedback UI =====
    private JLabel feedbackLabel;
    private Timer feedbackTimer;


    private final String username;
    private final Runnable onRestartGame;


    private final Runnable onQuitToMenu;

    // ===== Captured Pieces (UI only) =====
    private final List<AbstractPiece> capturedRed = new ArrayList<>();
    private final List<AbstractPiece> capturedBlack = new ArrayList<>();

    // ===== State =====
    private boolean isGameOver = false;
    private boolean isPaused = false;

    private final ChessBoardModel model;
    private final boolean isGuest;

    private AbstractPiece selectedPiece = null;
    private final List<Point> validMovePoints = new ArrayList<>();

    private Timer illegalMoveTimer;

    // ===== Overlays =====
    private PauseOverlayPanel pauseOverlay;
    private GameOverOverlayPanel gameOverOverlay;

    // ===== Top Bar =====
    private JLabel turnLabel;
    private JButton pauseButton;

    // ===== Geometry =====
    private static final int CELL_SIZE = 64;
    private static final int MARGIN = 40;
    private static final int PIECE_RADIUS = 25;

    // Space reserved for captured pieces on BOTH sides
    private static final int SIDE_PADDING = 140;

    // Board origin (top-left grid corner)
    private int boardLeftX() {
        return SIDE_PADDING + MARGIN;
    }

    private int boardTopY() {
        return MARGIN;
    }

    private int boardRightX() {
        return boardLeftX() + (ChessBoardModel.getCols() - 1) * CELL_SIZE;
    }

    private int boardBottomY() {
        return boardTopY() + (ChessBoardModel.getRows() - 1) * CELL_SIZE;
    }

    public ChessBoardPanel(ChessBoardModel model, boolean isGuest, String username, Runnable onRestartGame, Runnable onQuitToMenu) {
        this.model = model;
        this.isGuest = isGuest;
        this.username = username;
        this.onRestartGame = onRestartGame;
        this.onQuitToMenu = onQuitToMenu;

        // IMPORTANT: BorderLayout so NORTH/CENTER works
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(220, 179, 92));

        int boardWidth = (ChessBoardModel.getCols() - 1) * CELL_SIZE + MARGIN * 2;
        int boardHeight = (ChessBoardModel.getRows() - 1) * CELL_SIZE + MARGIN * 2;

        // Include side padding in preferred size so captured pieces don't get clipped
        setPreferredSize(new Dimension(boardWidth + SIDE_PADDING * 2, boardHeight));

        buildTopBar();
        hookMouse();
        rebuildCapturedFromModel();
    }

    private void buildTopBar() {
        turnLabel = new JLabel();
        turnLabel.setFont(new Font("Serif", Font.BOLD, 16));
        turnLabel.setForeground(Color.BLACK);
        updateTurnLabel();

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        feedbackLabel.setForeground(new Color(180, 0, 0));

        pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(e -> pauseGame());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(turnLabel);
        left.add(feedbackLabel);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(left, BorderLayout.WEST);
        topBar.add(pauseButton, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
    }

    private void showIllegalFeedback(String message) {
        feedbackLabel.setText(message);

        if (message.contains("Checkmate")) {
            feedbackLabel.setForeground(new Color(140, 0, 0));
        }
        else if (message.contains("Check")) {
            feedbackLabel.setForeground(new Color(200, 80, 0));
        }
        else if (message.contains("captured")) {
            feedbackLabel.setForeground(new Color(30, 120, 30));
        }
        else {
            feedbackLabel.setForeground(new Color(180, 0, 0));
        }

        if (feedbackTimer != null && feedbackTimer.isRunning()) {
            feedbackTimer.stop();
        }

        feedbackTimer = new Timer(1200, e -> {
            feedbackLabel.setText(" ");
            feedbackTimer.stop();
        });
        feedbackTimer.setRepeats(false);
        feedbackTimer.start();

        triggerIllegalMoveFeedback();
    }





    private void hookMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isPaused || isGameOver || turnLocked) return;
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void updateTurnLabel() {
        turnLabel.setText(model.isRedTurn() ? "Red to move" : "Black to move");
    }

    // ===================== Pause / Resume =====================

    public void pauseGame() {
        if (isPaused || isGameOver) return;
        isPaused = true;
        showPauseOverlay();
        repaint();
    }

    public void resumeGame() {
        if (isGameOver) return;
        isPaused = false;
        removePauseOverlay();
        repaint();
    }

    private void showPauseOverlay() {
        if (pauseOverlay != null) return;

        pauseOverlay = new PauseOverlayPanel(
                isGuest,
                model.isRedTurn(),
                e -> resumeGame(),
                e -> saveGame(),
                e -> restartGame(),
                e -> resignCurrentPlayer(),
                e -> quitToMenu()
        );

        add(pauseOverlay, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void removePauseOverlay() {
        if (pauseOverlay != null) {
            remove(pauseOverlay);
            pauseOverlay = null;
            revalidate();
            repaint();
        }
    }

    private void closePauseOverlayIfOpen() {
        if (pauseOverlay != null) {
            remove(pauseOverlay);
            pauseOverlay = null;
            revalidate();
            repaint();
        }
    }

    private void saveGame() {
        if (isGameOver) {
            JOptionPane.showMessageDialog(
                    this,
                    "Game has ended. Finished games cannot be saved.",
                    "Save Disabled",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (isGuest) {
            JOptionPane.showMessageDialog(this,
                    "Guests cannot save games.",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        model.saveGame("data/saves/" + username + ".save");
    }



    private void restartGame() {
        closePauseOverlayIfOpen();
        onRestartGame.run(); // new model, same screen
    }


    private void quitToMenu() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Quit to main menu?",
                "Confirm Quit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // === AUTOSAVE FOR LOGGED-IN USERS ===
        if (!isGuest && !isGameOver && username != null) {
            String path = "data/saves/" + username + ".save";
            model.saveGame(path);
        }

        if (onQuitToMenu != null) {
            onQuitToMenu.run();
        }
    }


    private void resignCurrentPlayer() {
        boolean resigningRed = model.isRedTurn();
        String resigningName = resigningRed ? "Red" : "Black";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                resigningName + " resigns. Are you sure?",
                "Confirm Resign",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        closePauseOverlayIfOpen();

        model.resignCurrentPlayer();

        deleteSaveIfExists();

        String msg = resigningRed
                ? "Black Wins! (Red resigned)"
                : "Red Wins! (Black resigned)";

        showGameOverOverlay(msg);

    }

    // ===================== Input / Move =====================

    private void handleMouseClick(int mouseX, int mouseY) {
        int col = Math.round((float) (mouseX - boardLeftX()) / CELL_SIZE);
        int row = Math.round((float) (mouseY - boardTopY()) / CELL_SIZE);

        if (!model.isValidPosition(row, col)) {
            clearSelection();
            showIllegalFeedback("Invalid position");
            repaint();
            return;
        }


        AbstractPiece clickedPiece = model.getPieceAt(row, col);

        if (selectedPiece == null) {
            if (clickedPiece != null && clickedPiece.isRed() == model.isRedTurn()) {
                selectedPiece = clickedPiece;
                calculateValidMovesPseudoLegal(selectedPiece);
            } else if (clickedPiece != null) {
                showIllegalFeedback("Not your turn");
            }
            repaint();
            return;
        }


        if (clickedPiece != null
                && clickedPiece.isRed() == model.isRedTurn()
                && clickedPiece.isRed() == selectedPiece.isRed()) {
            selectedPiece = clickedPiece;
            calculateValidMovesPseudoLegal(selectedPiece);
            repaint();
            return;
        }

        int fromRow = selectedPiece.getRow();
        int fromCol = selectedPiece.getCol();

        List<AbstractPiece> beforeMove = new ArrayList<>(model.getPieces());
        boolean ok = model.tryMove(fromRow, fromCol, row, col);

        clearSelection();
        updateTurnLabel();

        if (!ok) {
            if (model.generalInCheck(model.isRedTurn())) {
                showIllegalFeedback("Move exposes your General to check");
            } else {
                showIllegalFeedback("Illegal move");
            }
            repaint();
            return;
        }


        detectCapturedPieces(beforeMove);
        handlePostMoveUI();
        lockTurnBriefly();
        repaint();
    }

    private void detectCapturedPieces(List<AbstractPiece> beforeMove) {
        for (AbstractPiece p : beforeMove) {
            if (!model.getPieces().contains(p)) {

                if (p.isRed()) capturedRed.add(p);
                else capturedBlack.add(p);

                // === CAPTURE FEEDBACK ===
                String side = p.isRed() ? "Red" : "Black";
                showIllegalFeedback(side + " piece captured");
            }
        }
    }


    private void lockTurnBriefly() {
        turnLocked = true;

        animateTurnLabel();

        if (turnLockTimer != null && turnLockTimer.isRunning()) {
            turnLockTimer.stop();
        }

        turnLockTimer = new Timer(300, e -> {
            turnLocked = false;
            turnLockTimer.stop();
        });
        turnLockTimer.setRepeats(false);
        turnLockTimer.start();
    }

    private void animateTurnLabel() {
        if (turnAnimTimer != null && turnAnimTimer.isRunning()) {
            turnAnimTimer.stop();
        }

        final Color normal = Color.BLACK;
        final Color highlight = new Color(30, 120, 255);

        turnLabel.setForeground(highlight);

        turnAnimTimer = new Timer(250, e -> {
            turnLabel.setForeground(normal);
            turnAnimTimer.stop();
        });
        turnAnimTimer.setRepeats(false);
        turnAnimTimer.start();
    }



    private void rebuildCapturedFromModel() {
        capturedRed.clear();
        capturedBlack.clear();

        int redCount = 0;
        int blackCount = 0;

        for (AbstractPiece p : model.getPieces()) {
            if (p.isRed()) redCount++;
            else blackCount++;
        }

        int missingRed = 16 - redCount;
        int missingBlack = 16 - blackCount;

        // Optional: if you want exact piece types later, this can be refined
        for (int i = 0; i < missingRed; i++) {
            capturedRed.add(new SoldierPiece("兵", -1, -1, true));
        }
        for (int i = 0; i < missingBlack; i++) {
            capturedBlack.add(new SoldierPiece("卒", -1, -1, false));
        }
    }


    private void handlePostMoveUI() {
        String end = model.checkEndgame();

        // === CHECK (only if game continues) ===
        if ("NONE".equals(end)) {
            boolean opponentIsRed = model.isRedTurn();
            if (model.generalInCheck(opponentIsRed)) {
                showIllegalFeedback("Check!");
            }
            return;
        }

        // === GAME OVER ===
        deleteSaveIfExists();

        String message;

        if ("RED_WIN".equals(end)) {
            message = "Checkmate — Red Wins!";
        }
        else if ("BLACK_WIN".equals(end)) {
            message = "Checkmate — Black Wins!";
        }
        else {
            if (model.isThreefoldRepetition()) {
                message = "Draw — Threefold Repetition";
            }
            else if (model.isStalemate(model.isRedTurn())) {
                message = "Draw — Stalemate";
            }
            else {
                message = "Draw";
            }
        }

        showGameOverOverlay(message);
    }




    private void clearSelection() {
        selectedPiece = null;
        validMovePoints.clear();
    }

    private void calculateValidMovesPseudoLegal(AbstractPiece piece) {
        validMovePoints.clear();
        for (int r = 0; r < ChessBoardModel.getRows(); r++) {
            for (int c = 0; c < ChessBoardModel.getCols(); c++) {
                if (piece.canMoveTo(r, c, model)) {
                    validMovePoints.add(new Point(c, r));
                }
            }
        }
    }

    // ===================== Illegal Move Flash =====================

    private void triggerIllegalMoveFeedback() {
        if (illegalMoveTimer != null && illegalMoveTimer.isRunning()) return;

        illegalMoveTimer = new Timer(120, e -> {
            illegalMoveTimer.stop();
            repaint();
        });
        illegalMoveTimer.setRepeats(false);
        illegalMoveTimer.start();
        repaint();
    }

    // ===================== Paint =====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawBoard(g2d);
        drawLastMoveHighlightFromBackend(g2d);
        drawMoveHints(g2d);
        drawPieces(g2d);
        drawCheckIndicator(g2d);
        drawCapturedPieces(g2d);

        if (illegalMoveTimer != null && illegalMoveTimer.isRunning()) {
            g2d.setColor(new Color(200, 0, 0, 90));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawBoard(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));

        int left = boardLeftX();
        int top = boardTopY();
        int right = boardRightX();
        int bottom = boardBottomY();

        for (int i = 0; i < ChessBoardModel.getRows(); i++) {
            int y = top + i * CELL_SIZE;
            g.drawLine(left, y, right, y);
        }

        for (int i = 0; i < ChessBoardModel.getCols(); i++) {
            int x = left + i * CELL_SIZE;

            if (i == 0 || i == ChessBoardModel.getCols() - 1) {
                g.drawLine(x, top, x, bottom);
            } else {
                g.drawLine(x, top, x, top + 4 * CELL_SIZE);
                g.drawLine(x, top + 5 * CELL_SIZE, x, bottom);
            }
        }

        g.setFont(new Font("楷体", Font.BOLD, 24));
        int riverY = top + 4 * CELL_SIZE + CELL_SIZE / 2;

        String chuHe = "楚河";
        String hanJie = "汉界";
        FontMetrics fm = g.getFontMetrics();

        g.drawString(chuHe, left + CELL_SIZE * 2 - fm.stringWidth(chuHe) / 2, riverY + 8);
        g.drawString(hanJie, left + CELL_SIZE * 6 - fm.stringWidth(hanJie) / 2, riverY + 8);
    }

    private void drawPieces(Graphics2D g) {
        for (AbstractPiece piece : model.getPieces()) {
            int x = boardLeftX() + piece.getCol() * CELL_SIZE;
            int y = boardTopY() + piece.getRow() * CELL_SIZE;

            g.setColor(new Color(245, 222, 179));
            g.fillOval(x - PIECE_RADIUS, y - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - PIECE_RADIUS, y - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            if (piece == selectedPiece) drawCornerBorders(g, x, y);

            g.setFont(new Font("楷体", Font.BOLD, 22));
            g.setColor(piece.isRed() ? new Color(200, 0, 0) : Color.BLACK);

            FontMetrics fm = g.getFontMetrics();
            g.drawString(piece.getName(),
                    x - fm.stringWidth(piece.getName()) / 2,
                    y + fm.getAscent() / 2 - 2);
        }
    }

    private void drawMoveHints(Graphics2D g) {
        if (selectedPiece == null) return;

        for (Point p : validMovePoints) {
            AbstractPiece target = model.getPieceAt(p.y, p.x);

            // ❌ Skip marking friendly pieces entirely
            if (target != null && target.isRed() == selectedPiece.isRed()) {
                continue;
            }

            int cx = boardLeftX() + p.x * CELL_SIZE;
            int cy = boardTopY() + p.y * CELL_SIZE;

            if (target == null) {
                // 🟢 Normal move
                g.setColor(new Color(0, 180, 0, 120));
                g.fillOval(cx - 10, cy - 10, 20, 20);
            } else {
                // 🔴 Enemy capture
                g.setColor(new Color(200, 0, 0, 180));
                g.setStroke(new BasicStroke(3));
                g.drawOval(
                        cx - PIECE_RADIUS - 4,
                        cy - PIECE_RADIUS - 4,
                        (PIECE_RADIUS + 4) * 2,
                        (PIECE_RADIUS + 4) * 2
                );
            }
        }
    }


    private void drawCapturedPieces(Graphics2D g) {
        int miniRadius = 16;
        int spacing = 38;

        int startY = boardTopY() + 40;

        int xLeft = boardLeftX() - (SIDE_PADDING / 2);
        int xRight = boardRightX() + (SIDE_PADDING / 2);

        int y = startY;
        for (AbstractPiece p : capturedBlack) {
            drawMiniPiece(g, p, xLeft, y, miniRadius);
            y += spacing;
        }

        y = startY;
        for (AbstractPiece p : capturedRed) {
            drawMiniPiece(g, p, xRight, y, miniRadius);
            y += spacing;
        }
    }

    private void drawMiniPiece(Graphics2D g, AbstractPiece piece, int cx, int cy, int radius) {
        g.setColor(new Color(245, 222, 179));
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

        g.setFont(new Font("楷体", Font.BOLD, 16));
        g.setColor(piece.isRed() ? new Color(200, 0, 0) : Color.BLACK);

        FontMetrics fm = g.getFontMetrics();
        g.drawString(piece.getName(),
                cx - fm.stringWidth(piece.getName()) / 2,
                cy + fm.getAscent() / 2 - 2);
    }

    private void drawLastMoveHighlightFromBackend(Graphics2D g) {
        if (!model.hasLastMove()) return;

        Point from = new Point(model.getLastFromCol(), model.getLastFromRow());
        Point to = new Point(model.getLastToCol(), model.getLastToRow());

        g.setColor(new Color(255, 215, 0, 80));
        g.setStroke(new BasicStroke(3));
        drawSquareHighlight(g, from);
        drawSquareHighlight(g, to);
    }

    private void drawSquareHighlight(Graphics2D g, Point p) {
        int x = boardLeftX() + p.x * CELL_SIZE;
        int y = boardTopY() + p.y * CELL_SIZE;

        g.drawRect(
                x - CELL_SIZE / 2 + 4,
                y - CELL_SIZE / 2 + 4,
                CELL_SIZE - 8,
                CELL_SIZE - 8
        );
    }

    private AbstractPiece getCheckedGeneral() {
        if (model.generalInCheck(true)) {
            return model.findGeneral(true);   // red general
        }
        if (model.generalInCheck(false)) {
            return model.findGeneral(false);  // black general
        }
        return null;
    }

    private void drawCheckIndicator(Graphics2D g) {
        if (isPaused || isGameOver) return;

        AbstractPiece checked = getCheckedGeneral();
        if (checked == null) return;

        int cx = boardLeftX() + checked.getCol() * CELL_SIZE;
        int cy = boardTopY() + checked.getRow() * CELL_SIZE;

        g.setColor(new Color(220, 0, 0, 180));
        g.setStroke(new BasicStroke(4));

        g.drawOval(
                cx - PIECE_RADIUS - 6,
                cy - PIECE_RADIUS - 6,
                (PIECE_RADIUS + 6) * 2,
                (PIECE_RADIUS + 6) * 2
        );
    }

    private void deleteSaveIfExists() {
        if (isGuest || username == null) return;

        File saveFile = new File("data/saves/" + username + ".save");
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }



    private void drawCornerBorders(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(0, 100, 255));
        g.setStroke(new BasicStroke(3));

        int cs = 32, len = 12;

        g.drawLine(cx - cs, cy - cs, cx - cs + len, cy - cs);
        g.drawLine(cx - cs, cy - cs, cx - cs, cy - cs + len);

        g.drawLine(cx + cs, cy - cs, cx + cs - len, cy - cs);
        g.drawLine(cx + cs, cy - cs, cx + cs, cy - cs + len);

        g.drawLine(cx - cs, cy + cs, cx - cs + len, cy + cs);
        g.drawLine(cx - cs, cy + cs, cx - cs, cy + cs - len);

        g.drawLine(cx + cs, cy + cs, cx + cs - len, cy + cs);
        g.drawLine(cx + cs, cy + cs, cx + cs, cy + cs - len);
    }

    private void showGameOverOverlay(String message) {
        if (gameOverOverlay != null) return;

        isPaused = true;
        isGameOver = true;

        closePauseOverlayIfOpen();

        gameOverOverlay = new GameOverOverlayPanel(
                message,
                e -> restartGame(),
                e -> quitToMenu()
        );

        if (gameOverOverlay == null) {
            throw new IllegalStateException("GameOverOverlayPanel failed to initialize");
        }

        add(gameOverOverlay, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

}
