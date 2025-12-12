package edu.sustech.xiangqi.model;

public class GeneralPiece extends AbstractPiece {

    public GeneralPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed); // call the parent constructor and set initial position, name, and color
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {

        int currentRow = getRow();   // the row where the general is currently standing
        int currentCol = getCol();   // the column where the general is currently standing

        // 1️⃣ Cannot stay in the same place
        if (currentRow == targetRow && currentCol == targetCol) {
            return false;  // can't "move" to the same square
        }

        // 2️⃣ Check palace boundaries
        // Red palace: rows 7-9, columns 3-5
        // Black palace: rows 0-2, columns 3-5
        if (isRed()) { // if it's the red general
            if (targetRow < 7 || targetRow > 9 || targetCol < 3 || targetCol > 5) {
                return false; // outside palace = illegal
            }
        } else { // black general
            if (targetRow < 0 || targetRow > 2 || targetCol < 3 || targetCol > 5) {
                return false; // outside palace = illegal
            }
        }

        // 3️⃣ General can only move ONE step vertically or horizontally
        int rowDiff = Math.abs(targetRow - currentRow); // distance in rows
        int colDiff = Math.abs(targetCol - currentCol); // distance in columns

        // must move exactly one square in a straight line (not diagonal)
        if (rowDiff + colDiff != 1) {
            return false; // if movement is more than 1 step or diagonal → illegal
        }

        // 4️⃣ Check flying-general rule
        // If the target square lines up the two generals with no pieces in between → illegal
          AbstractPiece otherGeneral = model.findOtherGeneral(this); // method expected in ChessBoardModel

        // If they are in the same column after move attempt
        if (otherGeneral != null && targetCol == otherGeneral.getCol()) {
            // determine direction (up or down)
            int start = Math.min(targetRow, otherGeneral.getRow()) + 1;
            int end = Math.max(targetRow, otherGeneral.getRow());

            // check if there is ANY piece between them
            boolean blocked = false;
            for (int r = start; r < end; r++) {
                if (model.getPieceAt(r, targetCol) != null) { // there's something blocking the line
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                return false; // if no blocking piece → illegal flying general exposure
            }
        }
        return true; // if all checks pass, the move is legal
    }
}