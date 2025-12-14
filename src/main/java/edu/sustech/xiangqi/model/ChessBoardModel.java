package edu.sustech.xiangqi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;


public class ChessBoardModel {

    private final List<AbstractPiece> pieces; // stores every piece currently on the board
    private static final int ROWS = 10;       // Xiangqi has 10 rows
    private static final int COLS = 9;        // Xiangqi has 9 columns
    private boolean redTurn = true;           // true = red's turn, false = black's turn

    // last move (for UI highlighting)
    private int lastFromRow = -1;
    private int lastFromCol = -1;
    private int lastToRow = -1;
    private int lastToCol = -1;
    private boolean hasLastMove = false;

    // --- Game end state (for resign and UI) ---
    private boolean gameOver = false;
    private String gameResult = "NONE";    // "RED_WIN", "BLACK_WIN", "DRAW", "NONE"
    private String gameEndReason = null;   // e.g. "stalemate", "threefold_repetition", "resign"

    // --- Repetition Tracking ---
    private final Map<String, Integer> repetitionCounts = new HashMap<>();

    public ChessBoardModel() {
        pieces = new ArrayList<>();           // create the list
        initializePieces();                   // place all pieces in starting positions
    }

    private void initializePieces() {
        // ---------- Black side ----------
        pieces.add(new GeneralPiece("将", 0, 4, false));        // black general
        pieces.add(new AdvisorPiece("士", 0, 3, false));        // black advisors
        pieces.add(new AdvisorPiece("士", 0, 5, false));
        pieces.add(new ElephantPiece("象", 0, 2, false));       // black elephants
        pieces.add(new ElephantPiece("象", 0, 6, false));
        pieces.add(new HorsePiece("马", 0, 1, false));          // black horses
        pieces.add(new HorsePiece("马", 0, 7, false));
        pieces.add(new ChariotPiece("车", 0, 0, false));        // black chariots
        pieces.add(new ChariotPiece("车", 0, 8, false));
        pieces.add(new CannonPiece("炮", 2, 1, false));         // black cannons
        pieces.add(new CannonPiece("炮", 2, 7, false));

        // black soldiers (卒) on row 3, every 2 columns
        pieces.add(new SoldierPiece("卒", 3, 0, false));
        pieces.add(new SoldierPiece("卒", 3, 2, false));
        pieces.add(new SoldierPiece("卒", 3, 4, false));
        pieces.add(new SoldierPiece("卒", 3, 6, false));
        pieces.add(new SoldierPiece("卒", 3, 8, false));

        // ---------- Red side ----------
        pieces.add(new GeneralPiece("帅", 9, 4, true));         // red general
        pieces.add(new AdvisorPiece("仕", 9, 3, true));         // red advisors
        pieces.add(new AdvisorPiece("仕", 9, 5, true));
        pieces.add(new ElephantPiece("相", 9, 2, true));        // red elephants
        pieces.add(new ElephantPiece("相", 9, 6, true));
        pieces.add(new HorsePiece("马", 9, 1, true));           // red horses
        pieces.add(new HorsePiece("马", 9, 7, true));
        pieces.add(new ChariotPiece("车", 9, 0, true));         // red chariots
        pieces.add(new ChariotPiece("车", 9, 8, true));
        pieces.add(new CannonPiece("炮", 7, 1, true));          // red cannons
        pieces.add(new CannonPiece("炮", 7, 7, true));

        // red soldiers (兵) on row 6, every 2 columns
        pieces.add(new SoldierPiece("兵", 6, 0, true));
        pieces.add(new SoldierPiece("兵", 6, 2, true));
        pieces.add(new SoldierPiece("兵", 6, 4, true));
        pieces.add(new SoldierPiece("兵", 6, 6, true));
        pieces.add(new SoldierPiece("兵", 6, 8, true));
    }

    public List<AbstractPiece> getPieces() {
        return pieces;                        // return the list of all active pieces
    }

    public AbstractPiece getPieceAt(int row, int col) {
        for (AbstractPiece piece : pieces) {  // loop through all pieces
            if (piece.getRow() == row && piece.getCol() == col) {
                return piece;                // found a piece at this position
            }
        }
        return null;                         // empty square
    }

    public AbstractPiece findOtherGeneral(AbstractPiece current) { // used for flying-general rule
        for (AbstractPiece piece : pieces) {
            if (piece instanceof GeneralPiece       // must be a general
                    && piece != current             // not the same one
                    && piece.isRed() != current.isRed()) { // opposite side
                return piece;                       // return the enemy general
            }
        }
        return null;                                // should not happen in a normal game
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS              // inside board vertically
                && col >= 0 && col < COLS;         // inside board horizontally
    }

    // ---- check / simulation helpers ----
    private boolean lastMoveCausedCheck = false;     // optional: UI/test readouts
    private boolean lastMoveCausedCheckmate = false; // optional: UI/test readouts

    // find the general piece for a side (true = red, false = black)
    public AbstractPiece findGeneral(boolean isRed) {
        for (AbstractPiece p : pieces){
            if (p instanceof GeneralPiece && p.isRed() == isRed) {
                return p;
            }
        }
        return null;
    }

    // is the general of 'isRed' currently under attack?
    public boolean generalInCheck(boolean isRed) {
        AbstractPiece g = findGeneral(isRed);            // find the general
        if (g == null) {
            return false;
        }
        int gr = g.getRow(), gc = g.getCol();

        for (AbstractPiece p : pieces) {                 // check every enemy piece
            if (p.isRed() == isRed) {
                continue;            // skip same side
            }
            if (p.canMoveTo(gr, gc, this)) {
                return true;  // enemy can capture general
            }
        }
        return false;
    }

    // tiny simulator: move mover to (toR,toC), remove captured if any, run checkRed test, then revert
    private boolean simulateMoveCheck(AbstractPiece mover, int toR, int toC, boolean checkRed) {
        int fromR = mover.getRow(), fromC = mover.getCol();
        AbstractPiece captured = getPieceAt(toR, toC);   // maybe null
        if (captured != null) {
            pieces.remove(captured);   // remove captured for simulation
        }
        mover.moveTo(toR, toC);                          // simulate move

        boolean result = generalInCheck(checkRed);       // check condition on simulated board

        // revert simulation
        mover.moveTo(fromR, fromC);
        if (captured != null) {
            pieces.add(captured);     // restore captured piece
        }
        return result;
    }

    // would moving from->to leave mover's own general in check? (true => illegal)
    public boolean moveLeavesOwnGeneralInCheck(int fromR, int fromC, int toR, int toC) {
        AbstractPiece mover = getPieceAt(fromR, fromC);
        if (mover == null) {
            return false;
        }
        return simulateMoveCheck(mover, toR, toC, mover.isRed()); // check mover's general after the simulated move
    }

    // does the move cause the opponent's general to be in check?
    public boolean causesCheck(int fromR, int fromC, int toR, int toC) {
        AbstractPiece mover = getPieceAt(fromR, fromC);
        if (mover == null) {
            return false;
        }
        return simulateMoveCheck(mover, toR, toC, !mover.isRed()); // check opponent general after simulated move
    }

    // does the move cause checkmate? simulate move, if opponent in check then see if opponent has any legal escape
    public boolean causesCheckmate(int fromR, int fromC, int toR, int toC) {
        AbstractPiece mover = getPieceAt(fromR, fromC);
        if (mover == null) {
            return false;
        }

        // apply simulated mover move
        int origR = mover.getRow(), origC = mover.getCol();
        AbstractPiece captured = getPieceAt(toR, toC);
        if (captured != null) {
            pieces.remove(captured);
        }
        mover.moveTo(toR, toC);

        boolean opponentIsRed = !mover.isRed();
        boolean inCheck = generalInCheck(opponentIsRed);
        boolean isMate = false;

        if (inCheck) {
            isMate = true; // assume mate until an escape is found
            // iterate over a snapshot of pieces to avoid concurrent modification
            for (AbstractPiece enemy : new ArrayList<>(pieces)) {
                if (enemy.isRed() != opponentIsRed) {
                    continue; // only opponent pieces
                }
                int er = enemy.getRow(), ec = enemy.getCol();
                // try all board squares as candidate moves
                outer:
                for (int r = 0; r < ROWS; r++) {
                    for (int c = 0; c < COLS; c++) {
                        if (!enemy.canMoveTo(r, c, this)) {
                            continue; // not a legal piece move
                        }
                        // simulate enemy move
                        AbstractPiece cap2 = getPieceAt(r, c);
                        boolean removed = false;
                        if (cap2 != null) {
                            pieces.remove(cap2); removed = true; }
                        enemy.moveTo(r, c);

                        boolean stillInCheck = generalInCheck(opponentIsRed);

                        // revert enemy move
                        enemy.moveTo(er, ec);
                        if (removed && cap2 != null) {
                            pieces.add(cap2);
                        }

                        if (!stillInCheck) {
                            isMate = false; break outer; } // found escape -> not mate
                    }
                }
                if (!isMate) {
                    break; // found escape, stop checking others
                }
            }
        }

        // revert simulated mover move
        mover.moveTo(origR, origC);
        if (captured != null) {
            pieces.add(captured);
        }

        return inCheck && isMate;
    }

    // =============================================
// THREEFOLD REPETITION HELPERS
// =============================================

    /**
     * Convert board + player-to-move into a unique string.
     */
    private String serializePosition() {
        StringBuilder sb = new StringBuilder(ROWS * COLS + 2);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                AbstractPiece p = getPieceAt(r, c);
                if (p == null) {
                    sb.append('.');
                }
                else {
                    char code;
                    if (p instanceof GeneralPiece) code = 'K';
                    else if (p instanceof AdvisorPiece) code = 'A';
                    else if (p instanceof ElephantPiece) code = 'E';
                    else if (p instanceof HorsePiece) code = 'H';
                    else if (p instanceof ChariotPiece) code = 'R';
                    else if (p instanceof CannonPiece) code = 'C';
                    else if (p instanceof SoldierPiece) code = 'P';
                    else code = '?';
                    sb.append(p.isRed() ? code : Character.toLowerCase(code));
                }
            }
        }

        // add whose turn it is
        sb.append(redTurn ? " r" : " b");

        return sb.toString();
    }

    /** Store the current position, increasing its repetition counter. */
    private int recordCurrentPosition() {
        String key = serializePosition();
        int count = repetitionCounts.getOrDefault(key, 0) + 1;
        repetitionCounts.put(key, count);
        return count;
    }

    /** Return true if the current position has happened at least 3 times. */
    public boolean isThreefoldRepetition() {
        String key = serializePosition();
        return repetitionCounts.getOrDefault(key, 0) >= 3;
    }

    /** Reset the repetition data (call when starting a new game). */
    public void resetRepetitionCounts() {
        repetitionCounts.clear();
    }

    // RESIGN / SURRENDER HELPERS
    /** Mark the game as ended with a result and reason. */
    public void endGame(String result, String reason) {
        this.gameOver = true;
        this.gameResult = result;
        this.gameEndReason = reason;
        System.out.println("Game ended: " + result + " reason=" + reason);
    }

    /** Resign the player whose turn it currently is. */
    public void resignCurrentPlayer() {
        if (gameOver) return;
        boolean sideToMove = redTurn;
        String result = sideToMove ? "BLACK_WIN" : "RED_WIN";
        endGame(result, "resign");
    }

    /** Resign a specific color (true = red resigns, false = black resigns). */
    public void resignPlayer(boolean isRed) {
        if (gameOver) {
            return;
        }
        String result = isRed ? "BLACK_WIN" : "RED_WIN";
        endGame(result, "resign");
    }


    // SAVE GAME (TASK 2)

    public void saveGame(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {

            out.println("# Xiangqi Save File");

            for (AbstractPiece p : pieces) {
                String code;
                if (p instanceof GeneralPiece) {
                    code = "K";
                }
                else if (p instanceof AdvisorPiece) {
                    code = "A";
                }
                else if (p instanceof ElephantPiece) {
                    code = "E";
                }
                else if (p instanceof HorsePiece) {
                    code = "H";
                }
                else if (p instanceof ChariotPiece) {
                    code = "R";
                }
                else if (p instanceof CannonPiece) {
                    code = "C";
                }
                else if (p instanceof SoldierPiece) {
                    code = "P";
                }
                else {
                    code = "?";
                }

                out.println("PIECE " + code + " "
                        + p.getRow() + " "
                        + p.getCol() + " "
                        + p.isRed());
            }

            // save whose turn it is
            out.println("TURN " + redTurn);

        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    // LOAD GAME (TASK 2)

    public void loadGame(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            // clear current board
            pieces.clear();

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] arr = line.split(" ");

                if (arr[0].equals("PIECE")) {
                    String code = arr[1];
                    int row = Integer.parseInt(arr[2]);
                    int col = Integer.parseInt(arr[3]);
                    boolean isRed = Boolean.parseBoolean(arr[4]);

                    AbstractPiece p = null;
                    switch (code) {
                        case "K":
                            p = new GeneralPiece(isRed ? "帅" : "将", row, col, isRed);
                            break;
                        case "A":
                            p = new AdvisorPiece(isRed ? "仕" : "士", row, col, isRed);
                            break;
                        case "E":
                            p = new ElephantPiece(isRed ? "相" : "象", row, col, isRed);
                            break;
                        case "H":
                            p = new HorsePiece("马", row, col, isRed);
                            break;
                        case "R":
                            p = new ChariotPiece("车", row, col, isRed);
                            break;
                        case "C":
                            p = new CannonPiece("炮", row, col, isRed);
                            break;
                        case "P":
                            p = new SoldierPiece(isRed ? "兵" : "卒", row, col, isRed);
                            break;
                    }


                    if (p != null) {
                        pieces.add(p);
                    }
                }

                else if (arr[0].equals("TURN")) {
                    redTurn = Boolean.parseBoolean(arr[1]);
                }
            }

            repetitionCounts.clear();
            System.out.println("Game loaded successfully.");

        } catch (Exception e) {
            System.out.println("Error loading game: " + e.getMessage());
            System.out.println("Save file may be corrupted.");
        }
    }


    // TASK 3: AUTO-SAVE ON EXIT
    /**
     * Automatically save the game when the program exits.
     * This uses a shutdown hook, meaning when the application closes,
     * this code will run once and call saveGame("autosave.txt").
     */
    public void enableAutoSaveOnExit() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Auto-saving game before exit...");
            saveGame("autosave.txt");   // save game to a fixed file
        }));
    }

    // STALEMATE + ENDGAME CODE
    /**
     * Return true if the given side (isRed) has at least one legal move.
     *
     * A legal move must:
     *  1) obey the piece's own movement rules (handled by canMoveTo)
     *  2) not capture a friendly piece
     *  3) not leave the moving side's General in check after the move
     *
     * This method tries every destination square for every piece of the given side
     * and returns true as soon as it finds one legal move.
     */
    public boolean hasLegalMove(boolean isRed) {
        // Iterate over a snapshot of pieces to avoid concurrent modification if any external code mutates pieces
        for (AbstractPiece p : new ArrayList<>(pieces)) {
            if (p == null) {
                continue;
            }
            if (p.isRed() != isRed) {
                continue; // only consider pieces of the side we're checking
            }

            int sr = p.getRow();
            int sc = p.getCol();

            // Try every board square as a candidate destination
            for (int tr = 0; tr < ROWS; tr++) {
                for (int tc = 0; tc < COLS; tc++) {

                    // 1) Piece-specific movement rules
                    if (!p.canMoveTo(tr, tc, this)) {
                        continue;
                    }

                    // 2) Can't capture your own piece
                    AbstractPiece target = getPieceAt(tr, tc);
                    if (target != null && target.isRed() == isRed) {
                        continue;
                    }

                    // 3) If this move would leave our own general in check, it's illegal.
                    //    moveLeavesOwnGeneralInCheck(fromR, fromC, toR, toC) should be an existing helper in your model.
                    //    If your helper has a different name, substitute it here.
                    if (moveLeavesOwnGeneralInCheck(sr, sc, tr, tc)) {
                        continue;
                    }

                    // All checks passed -> found a legal move
                    return true;
                }
            }
        }
        // No legal move found
        return false;
    }

    /**
     * Returns true if the player 'isRed' is in stalemate.
     * Stalemate definition in Xiangqi:
     *   - The player's general is NOT in check
     *   - The player has NO legal moves
     *
     * In Xiangqi stalemate = DRAW.
     */
    public boolean isStalemate(boolean isRed) {
        // If in check, it's not stalemate (it might be checkmate)
        if (generalInCheck(isRed)) {
            return false;
        }

        // If there is at least one legal move, not stalemate
        if (hasLegalMove(isRed)) {
            return false;
        }

        // Not in check and no legal moves => stalemate
        return true;
    }

    /**
     * Unified endgame checker to call after a move completes (after you switch turn).
     * Returns one of:
     *   - "RED_WIN", "BLACK_WIN", "DRAW", "NONE"
     *
     * It uses:
     *  - generalInCheck(side) : whether the side's general is currently in check
     *  - hasLegalMove(side) : whether the side has any legal move
     *
     * Important: call this AFTER you have updated redTurn (the player to move) so sideToMove is accurate.
     */
    public String checkEndgame() {
        boolean sideToMove = redTurn; // true if red to move, false if black to move

        // 1) Checkmate: side-to-move IN check AND has NO legal moves
        if (generalInCheck(sideToMove) && !hasLegalMove(sideToMove)) {
            // sideToMove is checkmated -> opponent wins
            if (sideToMove) {
                return "BLACK_WIN"; // red was to move and got checkmated => black wins
            }
            else return "RED_WIN";
        }

        // 2) Stalemate (Xiangi): side-to-move NOT in check AND has NO legal moves -> DRAW
        if (!generalInCheck(sideToMove) && !hasLegalMove(sideToMove)) {
            return "DRAW"; // stalemate
        }

        // Threefold repetition rule
        if (isThreefoldRepetition()) {
            return "DRAW";
        }

        // 3) Optional other draw rules (repetition, dead position) can be added here.
        // Example placeholders (implement those methods if you want):
        // if (isThreefoldRepetition()) return "DRAW";
        // if (isDeadPosition()) return "DRAW";

        // No end condition reached
        return "NONE";
    }

    public boolean movePiece(AbstractPiece piece, int newRow, int newCol) {
        /**
     * Main move method used by the UI.
     * It checks:
     * - board boundaries
     * - whose turn it is
     * - cannot capture your own piece
     * - piece movement rules (canMoveTo)
     * - capturing (removing target from list)
     * - updates the piece position
     * - switches the turn
     */
        if (piece == null) {
            return false;                        // no piece selected
        }
        if (!isValidPosition(newRow, newCol)) {
            return false;     // outside board
        }
        if (piece.isRed() != redTurn) {
            return false;             // wrong player's turn
        }

        AbstractPiece target = getPieceAt(newRow, newCol);      // piece at destination, if any

        // cannot capture your own piece
        if (target != null && target.isRed() == piece.isRed()) {
            return false;
        }

        // piece-specific movement rules
        if (!piece.canMoveTo(newRow, newCol, this)) {
            return false;
        }

        //do not allow moves that leave your own general in check
        if (moveLeavesOwnGeneralInCheck(piece.getRow(), piece.getCol(), newRow, newCol)) {
            return false;
        }

        // remember origin for check/checkmate checks later
        int origRow = piece.getRow();
        int origCol = piece.getCol();

        // record last-move "from" for UI
        lastFromRow = origRow;
        lastFromCol = origCol;

        // *** simulate whether this move would cause check/checkmate BEFORE applying it to the real board ***
        // (causesCheck / causesCheckmate expect the mover to still be at (origRow,origCol))
        boolean causedCheck = causesCheck(origRow, origCol, newRow, newCol);
        boolean causedCheckmate = causesCheckmate(origRow, origCol, newRow, newCol);

        // now perform the actual capture and move (apply to the real board)
        if (target != null) {
            pieces.remove(target);
        }
        piece.moveTo(newRow, newCol);

        // now update last-move fields with the previously computed results
        lastMoveCausedCheck = causedCheck;
        lastMoveCausedCheckmate = causedCheckmate;

        // record last-move "to" for UI highlighting
        lastToRow = newRow;
        lastToCol = newCol;
        hasLastMove = true;

        // switch turn
        redTurn = !redTurn;

        // --- record repetition after switching turn ---
        int occurrences = recordCurrentPosition();
        if (occurrences >= 3) {
            // This is a draw by threefold repetition
            // You can notify UI here or let checkEndgame() handle it
            System.out.println("Draw by repetition");
        }

        // Now check the endgame condition
        String end = checkEndgame();
        if ("RED_WIN".equals(end)) {
            // Notify UI / set game state: Red wins
            // e.g., gameResult = GameResult.RED_WIN; emitGameEnd(...);
        }
        else if ("BLACK_WIN".equals(end)) {
            // Notify UI / set game state: Black wins
        }
        else if ("DRAW".equals(end)) {
            // Notify UI that game ended in a draw. Determine reason if needed:
            if (isStalemate(redTurn)) {
                // send { result:"DRAW", reason:"stalemate" } to frontend
            } else if (/* check repetition or dead-position */ false) {
                // send appropriate reason field
            } else {
                // generic draw
            }
        }
        else {
            // "NONE" -> game continues
        }
        return true;
    }

    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        AbstractPiece piece = getPieceAt(fromRow, fromCol); // find the selected piece
        return movePiece(piece, toRow, toCol);              // use the main move logic
    }

    public boolean isRedTurn() {
        return redTurn;                            // tells whose turn it is (useful for UI or debugging)
    }

    public static int getRows() {
        return ROWS;                               // total number of rows on the board
    }

    public static int getCols() {
        return COLS;                               // total number of columns on the board
    }

    // last move getters for UI display
    public boolean hasLastMove() { return hasLastMove; }

    public int getLastFromRow() { return lastFromRow; }
    public int getLastFromCol() { return lastFromCol; }

    public int getLastToRow() { return lastToRow; }
    public int getLastToCol() { return lastToCol; }
}
