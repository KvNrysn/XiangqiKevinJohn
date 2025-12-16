package edu.sustech.xiangqi.ui;

import edu.sustech.xiangqi.audio.AudioManager;
import edu.sustech.xiangqi.model.AbstractPiece;
import edu.sustech.xiangqi.model.ChessBoardModel;
import edu.sustech.xiangqi.model.SoldierPiece;
import edu.sustech.xiangqi.model.ChessBoardModel.Move;



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.io.File;

public class ChessBoardPanel extends JPanel {

    private JPanel topBar;
    private JPanel hudPanel;

    private JLayeredPane layeredPane;
    private JPanel basePanel;

    // ===== Move History =====
    private MoveHistoryPanel moveHistoryPanel;
    private boolean historyVisible = false;
    private Timer historySlideTimer;
    private int historyOffsetX = 0;
    private static final int HISTORY_WIDTH = 320;



    // ===== UI Colors =====
    private static final Color GOLD = new Color(212, 175, 55);


    // ===== Turn lock / animation =====
    private boolean turnLocked = false;
    private Timer turnLockTimer;
    private Timer turnAnimTimer;

    // ===== Move Animation =====
    private static final int MOVE_ANIM_DURATION_MS = 180;
    private static final int MOVE_ANIM_TICK_MS = 16; // ~60fps
    private boolean moveAnimating = false;
    private Timer moveAnimTimer;
    private AbstractPiece animPiece = null;
    private int animFromRow, animFromCol, animToRow, animToCol;
    private float animT = 0f;
    private List<AbstractPiece> animBeforeMoveSnapshot = null; // for capture detection after animation

    // ===== Captured fade-in animation =====
    private static final int CAPTURE_FADE_MS = 260;
    private static final int CAPTURE_FADE_TICK_MS = 16;
    private Timer captureFadeTimer;

    private static class CapturedEntry {
        final AbstractPiece piece;
        float alpha; // 0..1
        CapturedEntry(AbstractPiece piece, float alpha) {
            this.piece = piece;
            this.alpha = alpha;
        }
    }

    // ===== Feedback UI =====
    private JLabel feedbackLabel;
    private Timer feedbackTimer;

    private final String username;
    private final Runnable onRestartGame;
    private final Runnable onQuitToMenu;

    // ===== Captured Pieces (UI only) =====
    private final List<CapturedEntry> capturedRed = new ArrayList<>();
    private final List<CapturedEntry> capturedBlack = new ArrayList<>();

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
        moveHistoryPanel = new MoveHistoryPanel();
        moveHistoryPanel.setPreferredSize(new Dimension(0, getHeight()));
        moveHistoryPanel.setVisible(false);
        add(moveHistoryPanel, BorderLayout.EAST);

        this.model = model;
        this.isGuest = isGuest;
        this.username = username;
        this.onRestartGame = onRestartGame;
        this.onQuitToMenu = onQuitToMenu;

        setLayout(new BorderLayout());

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        basePanel = new JPanel(new BorderLayout());
        basePanel.setOpaque(false);

        layeredPane.add(basePanel, JLayeredPane.DEFAULT_LAYER);
        add(layeredPane, BorderLayout.CENTER);

        setOpaque(true);
        setBackground(new Color(220, 179, 92));

        int boardWidth = (ChessBoardModel.getCols() - 1) * CELL_SIZE + MARGIN * 2;
        int boardHeight = (ChessBoardModel.getRows() - 1) * CELL_SIZE + MARGIN * 2;

        setPreferredSize(new Dimension(boardWidth + SIDE_PADDING * 2, boardHeight));

        buildTopBar();
        moveHistoryPanel = new MoveHistoryPanel();
        moveHistoryPanel.setVisible(false);
        basePanel.add(moveHistoryPanel, BorderLayout.EAST);
        hookMouse();
        rebuildCapturedFromModel();
    }

    private void buildTopBar() {

        // ===== Turn label =====
        turnLabel = new JLabel();
        turnLabel.setFont(new Font("Serif", Font.BOLD, 16));
        turnLabel.setForeground(GOLD);
        updateTurnLabel();

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        feedbackLabel.setForeground(new Color(180, 0, 0));

        // ===== HISTORY button (left) =====
        JButton historyButton = createGoldButton("HISTORY");
        historyButton.addActionListener(e -> toggleHistoryPanel());

        // ===== PAUSE button (right) =====
        pauseButton = createGoldButton("PAUSE");
        pauseButton.addActionListener(e -> {
            AudioManager.playSFX("click");
            pauseGame();
        });

        // ===== Left column =====
        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(historyButton);
        leftColumn.add(Box.createVerticalStrut(6));
        leftColumn.add(turnLabel);
        leftColumn.add(feedbackLabel);

        // ===== Right column =====
        JPanel rightColumn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightColumn.setOpaque(false);
        rightColumn.add(pauseButton);

        // ===== HUD panel =====
        hudPanel = new JPanel(new BorderLayout());
        hudPanel.setOpaque(false);
        hudPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 0, 16));
        hudPanel.add(leftColumn, BorderLayout.WEST);
        hudPanel.add(rightColumn, BorderLayout.EAST);

        layeredPane.add(hudPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private JButton createGoldButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 16;

                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(2, 3, w - 4, h - 4, r, r);

                g2.setColor(new Color(245, 235, 210));
                g2.fillRoundRect(0, 0, w - 2, h - 2, r, r);

                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(GOLD);
                g2.drawRoundRect(0, 0, w - 2, h - 2, r, r);

                g2.setColor(Color.BLACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(
                        getText(),
                        (w - fm.stringWidth(getText())) / 2,
                        (h + fm.getAscent()) / 2 - 2
                );
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(110, 32));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }






    private void showIllegalFeedback(String message) {
        feedbackLabel.setText(message);

        if (message.contains("Checkmate")) {
            feedbackLabel.setForeground(new Color(212, 175, 55)); // gold
            AudioManager.playSFX("gameover");
        }
        else if (message.contains("Check")) {
            feedbackLabel.setForeground(new Color(230, 200, 120)); // soft gold
            AudioManager.playSFX("check");
        }
        else if (message.contains("captured")) {
            feedbackLabel.setForeground(new Color(200, 190, 140)); // muted gold
            AudioManager.playSFX("capture");
        }
        else {
            feedbackLabel.setForeground(new Color(220, 200, 140)); // warning gold
            AudioManager.playSFX("illegal");
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

        if (!message.contains("Check") && !message.contains("captured")) {
            triggerIllegalMoveFeedback();
        }
    }

    private void toggleHistoryPanel() {
        if (historySlideTimer != null && historySlideTimer.isRunning()) return;

        historyVisible = !historyVisible;

        if (historyVisible) {
            historyOffsetX = 0;
            moveHistoryPanel.setVisible(true);
        }

        int end = historyVisible ? HISTORY_WIDTH : 0;

        historySlideTimer = new Timer(16, e -> {

            // move toward "end"
            if (historyOffsetX < end) {
                historyOffsetX = Math.min(end, historyOffsetX + 24);
            } else if (historyOffsetX > end) {
                historyOffsetX = Math.max(end, historyOffsetX - 24);
            }

            moveHistoryPanel.setPreferredSize(new Dimension(historyOffsetX, getHeight()));
            moveHistoryPanel.revalidate();   // important: revalidate the panel itself too
            revalidate();
            repaint();

            if (historyOffsetX == end) {
                historySlideTimer.stop();
                if (!historyVisible) moveHistoryPanel.setVisible(false);
            }
        });


        historySlideTimer.start();
    }




    private void hookMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isPaused || isGameOver || turnLocked || moveAnimating) return;
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void updateTurnLabel() {
        turnLabel.setText(model.isRedTurn() ? "Red to move" : "Black to move");
    }

    @Override
    public void doLayout() {
        super.doLayout();

        layeredPane.setBounds(0, 0, getWidth(), getHeight());
        basePanel.setBounds(0, 0, getWidth(), getHeight());

        if (pauseOverlay != null) {
            pauseOverlay.setBounds(0, 0, getWidth(), getHeight());
        }
        if (gameOverOverlay != null) {
            gameOverOverlay.setBounds(0, 0, getWidth(), getHeight());
        }
        if (hudPanel != null) {
            hudPanel.setBounds(0, 0, getWidth(), 80);
        }

    }


    // ===================== Pause / Resume =====================

    public void pauseGame() {
        if (isPaused || isGameOver || moveAnimating) return;
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
        onRestartGame.run();
    }

    private void quitToMenu() {
        AudioManager.playSFX("click");

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Quit to main menu?",
                "Confirm Quit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

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

        AudioManager.playSFX("click");

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
        for (Point p : validMovePoints) {
            int cx = boardLeftX() + p.x * CELL_SIZE;
            int cy = boardTopY() + p.y * CELL_SIZE;

            double dx = mouseX - cx;
            double dy = mouseY - cy;

            if (Math.sqrt(dx * dx + dy * dy) <= 14) {
                attemptMoveTo(p.y, p.x);
                return;
            }
        }

        Point cell = getCellFromMouse(mouseX, mouseY);
        if (cell == null) {
            clearSelection();
            showIllegalFeedback("Invalid position");
            repaint();
            return;
        }

        int col = cell.x;
        int row = cell.y;

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

        AbstractPiece movingPiece = selectedPiece;
        int fromRow = movingPiece.getRow();
        int fromCol = movingPiece.getCol();

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

        startMoveAnimation(movingPiece, fromRow, fromCol, row, col, beforeMove);
    }

    private void attemptMoveTo(int row, int col) {
        if (selectedPiece == null) return;

        AbstractPiece movingPiece = selectedPiece;
        int fromRow = movingPiece.getRow();
        int fromCol = movingPiece.getCol();

        List<AbstractPiece> beforeMove = new ArrayList<>(model.getPieces());
        boolean ok = model.tryMove(fromRow, fromCol, row, col);

        clearSelection();
        updateTurnLabel();

        if (!ok) {
            showIllegalFeedback("Illegal move");
            return;
        }

        startMoveAnimation(movingPiece, fromRow, fromCol, row, col, beforeMove);
    }

    // ===================== MOVE ANIMATION =====================

    private void startMoveAnimation(AbstractPiece piece, int fr, int fc, int tr, int tc, List<AbstractPiece> beforeMove) {
        if (moveAnimTimer != null && moveAnimTimer.isRunning()) {
            moveAnimTimer.stop();
        }

        moveAnimating = true;
        animPiece = piece;
        animFromRow = fr;
        animFromCol = fc;
        animToRow = tr;
        animToCol = tc;
        animT = 0f;
        animBeforeMoveSnapshot = beforeMove;

        moveAnimTimer = new Timer(MOVE_ANIM_TICK_MS, e -> {
            animT += (float) MOVE_ANIM_TICK_MS / MOVE_ANIM_DURATION_MS;
            if (animT >= 1f) {
                animT = 1f;
                moveAnimTimer.stop();
                finishMoveAnimation();
            }
            repaint();
        });

        moveAnimTimer.start();
    }

    private void finishMoveAnimation() {
        moveAnimating = false;

        AudioManager.playSFX("move");

        if (animBeforeMoveSnapshot != null) {
            detectCapturedPieces(animBeforeMoveSnapshot);
            animBeforeMoveSnapshot = null;
        }

        if (moveHistoryPanel != null) {
            moveHistoryPanel.updateMoves(model.getMoveHistory());
        }


        handlePostMoveUI();
        lockTurnBriefly();

        animPiece = null;


        repaint();

    }


    private void detectCapturedPieces(List<AbstractPiece> beforeMove) {
        boolean addedAny = false;

        for (AbstractPiece p : beforeMove) {
            if (!model.getPieces().contains(p)) {

                // add as fade-in entry
                if (p.isRed()) capturedRed.add(new CapturedEntry(p, 0f));
                else capturedBlack.add(new CapturedEntry(p, 0f));
                addedAny = true;

                AudioManager.playSFX("capture");

                String side = p.isRed() ? "Red" : "Black";
                showIllegalFeedback(side + " piece captured");
            }
        }

        if (addedAny) {
            startCaptureFadeTimer();
        }
    }

    private void startCaptureFadeTimer() {
        if (captureFadeTimer != null && captureFadeTimer.isRunning()) return;

        captureFadeTimer = new Timer(CAPTURE_FADE_TICK_MS, e -> {
            boolean stillAnimating = false;
            float step = (float) CAPTURE_FADE_TICK_MS / CAPTURE_FADE_MS;

            for (CapturedEntry ce : capturedRed) {
                if (ce.alpha < 1f) {
                    ce.alpha = Math.min(1f, ce.alpha + step);
                    if (ce.alpha < 1f) stillAnimating = true;
                }
            }
            for (CapturedEntry ce : capturedBlack) {
                if (ce.alpha < 1f) {
                    ce.alpha = Math.min(1f, ce.alpha + step);
                    if (ce.alpha < 1f) stillAnimating = true;
                }
            }

            repaint();

            if (!stillAnimating) {
                captureFadeTimer.stop();
            }
        });

        captureFadeTimer.start();
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

        final Color normal = GOLD;
        final Color highlight = new Color(255, 215, 120);

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

        // These are placeholders, treat as already visible (alpha=1)
        for (int i = 0; i < missingRed; i++) {
            capturedRed.add(new CapturedEntry(new SoldierPiece("兵", -1, -1, true), 1f));
        }
        for (int i = 0; i < missingBlack; i++) {
            capturedBlack.add(new CapturedEntry(new SoldierPiece("卒", -1, -1, false), 1f));
        }
    }

    private void handlePostMoveUI() {
        String end = model.checkEndgame();

        if ("NONE".equals(end)) {
            boolean opponentIsRed = model.isRedTurn();
            if (model.generalInCheck(opponentIsRed)) {
                showIllegalFeedback("Check!");
                AudioManager.playSFX("check");
            }
            return;
        }

        deleteSaveIfExists();

        String message;
        if ("RED_WIN".equals(end)) {
            message = "Checkmate — Red Wins!";
        } else if ("BLACK_WIN".equals(end)) {
            message = "Checkmate — Black Wins!";
        } else {
            if (model.isThreefoldRepetition()) {
                message = "Draw — Threefold Repetition";
            } else if (model.isStalemate(model.isRedTurn())) {
                message = "Draw — Stalemate";
            } else {
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

        GradientPaint bg = new GradientPaint(
                0, 0, new Color(12, 12, 12),
                0, getHeight(), new Color(6, 6, 6)
        );
        g2d.setPaint(bg);
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
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(1));

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

        // ===== Palace diagonals (gold) =====
        g.setStroke(new BasicStroke(1));
        g.setColor(GOLD);

// Red palace (bottom)
        int rpTop = boardTopY() + 7 * CELL_SIZE;
        int rpLeft = boardLeftX() + 3 * CELL_SIZE;
        int rpRight = boardLeftX() + 5 * CELL_SIZE;
        int rpBottom = boardTopY() + 9 * CELL_SIZE;

        g.drawLine(rpLeft, rpTop, rpRight, rpBottom);
        g.drawLine(rpRight, rpTop, rpLeft, rpBottom);

// Black palace (top)
        int bpTop = boardTopY();
        int bpLeft = boardLeftX() + 3 * CELL_SIZE;
        int bpRight = boardLeftX() + 5 * CELL_SIZE;
        int bpBottom = boardTopY() + 2 * CELL_SIZE;

        g.drawLine(bpLeft, bpTop, bpRight, bpBottom);
        g.drawLine(bpRight, bpTop, bpLeft, bpBottom);
    }

    private void drawPieces(Graphics2D g) {
        for (AbstractPiece piece : model.getPieces()) {

            int cx;
            int cy;

            if (moveAnimating && piece == animPiece) {
                int sx = boardLeftX() + animFromCol * CELL_SIZE;
                int sy = boardTopY() + animFromRow * CELL_SIZE;
                int ex = boardLeftX() + animToCol * CELL_SIZE;
                int ey = boardTopY() + animToRow * CELL_SIZE;

                cx = (int) (sx + (ex - sx) * animT);
                cy = (int) (sy + (ey - sy) * animT);
            } else {
                cx = boardLeftX() + piece.getCol() * CELL_SIZE;
                cy = boardTopY() + piece.getRow() * CELL_SIZE;
            }

            g.setColor(new Color(245, 222, 179));
            g.fillOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

            if (piece == selectedPiece) drawCornerBorders(g, cx, cy);

            g.setFont(new Font("楷体", Font.BOLD, 22));
            g.setColor(piece.isRed() ? new Color(200, 0, 0) : Color.BLACK);

            FontMetrics fm = g.getFontMetrics();
            g.drawString(piece.getName(),
                    cx - fm.stringWidth(piece.getName()) / 2,
                    cy + fm.getAscent() / 2 - 2);
        }
    }

    private void drawMoveHints(Graphics2D g) {
        if (selectedPiece == null) return;

        for (Point p : validMovePoints) {
            AbstractPiece target = model.getPieceAt(p.y, p.x);

            if (target != null && target.isRed() == selectedPiece.isRed()) {
                continue;
            }

            int cx = boardLeftX() + p.x * CELL_SIZE;
            int cy = boardTopY() + p.y * CELL_SIZE;

            if (target == null) {
                g.setColor(new Color(0, 180, 0, 120));
                g.fillOval(cx - 10, cy - 10, 20, 20);
            } else {
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
        for (CapturedEntry ce : capturedBlack) {
            drawMiniPiece(g, ce, xLeft, y, miniRadius);
            y += spacing;
        }

        y = startY;
        for (CapturedEntry ce : capturedRed) {
            drawMiniPiece(g, ce, xRight, y, miniRadius);
            y += spacing;
        }
    }

    private void drawMiniPiece(Graphics2D g, CapturedEntry entry, int cx, int cy, int radius) {
        // apply fade alpha (only for this mini piece)
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, entry.alpha))));

        AbstractPiece piece = entry.piece;

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

        g.setComposite(old);
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
            return model.findGeneral(true);
        }
        if (model.generalInCheck(false)) {
            return model.findGeneral(false);
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

    private Point getCellFromMouse(int mouseX, int mouseY) {
        int left = boardLeftX();
        int top = boardTopY();

        for (int r = 0; r < ChessBoardModel.getRows(); r++) {
            for (int c = 0; c < ChessBoardModel.getCols(); c++) {
                int cx = left + c * CELL_SIZE;
                int cy = top + r * CELL_SIZE;

                double dx = mouseX - cx;
                double dy = mouseY - cy;

                if (Math.sqrt(dx * dx + dy * dy) <= PIECE_RADIUS + 10) {
                    return new Point(c, r);
                }
            }
        }
        return null;
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

        AudioManager.playSFX("gameover");
        gameOverOverlay.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(gameOverOverlay, JLayeredPane.MODAL_LAYER);
        layeredPane.repaint();
        revalidate();
        repaint();
    }
}
