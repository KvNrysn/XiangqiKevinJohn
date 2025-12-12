package edu.sustech.xiangqi.model;
/**
 * 相 / 象
 */
public class ElephantPiece extends AbstractPiece {

    public ElephantPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed); // set name, position, and side
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();   // current row
        int currentCol = getCol();   // current column

        if (currentRow == targetRow && currentCol == targetCol) {
            return false; // no move
        }

        int rowDiff = targetRow - currentRow;           // signed row diff
        int colDiff = targetCol - currentCol;           // signed col diff
        int absRowDiff = Math.abs(rowDiff);             // |rowDiff|
        int absColDiff = Math.abs(colDiff);             // |colDiff|

        // (exactly 2 steps diagonally)
        if (!(absRowDiff == 2 && absColDiff == 2)) {
            return false; // false if not a 2-step diagonal
        }

        // 相/象不过河
        // Red elephant must stay on rows 5-9 (can't go above 4)
        // Black elephant must stay on rows 0-4 (can't go below 5)
        if (isRed()) { // red side
            if (targetRow < 5) {
                return false; // crossed the river → illegal
            }
        } else { // black side
            if (targetRow > 4) {
                return false; // crossed the river → illegal
            }
        }

        // (the middle diagonal square must be empty)
        int midRow = (currentRow + targetRow) / 2;      // middle row
        int midCol = (currentCol + targetCol) / 2;      // middle col

        if (model.getPieceAt(midRow, midCol) != null) {
            return false; // blocked elephant eye
        }

        return true; // move is legal by elephant rules
    }
}
