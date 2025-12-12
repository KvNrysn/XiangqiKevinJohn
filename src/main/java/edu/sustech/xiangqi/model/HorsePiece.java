package edu.sustech.xiangqi.model;

/**
 * 马
 */
public class HorsePiece extends AbstractPiece {

    public HorsePiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed); // pass to base class
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();   // current row
        int currentCol = getCol();   // current column

        if (currentRow == targetRow && currentCol == targetCol) {
            return false; // same place
        }

        int rowDiff = targetRow - currentRow;           // signed row diff
        int colDiff = targetCol - currentCol;           // signed col diff
        int absRowDiff = Math.abs(rowDiff);             // |rowDiff|
        int absColDiff = Math.abs(colDiff);             // |colDiff|

        // The horse moves in the shape of the Chinese character 日: one step straight and one step diagonal — either (1,2) or (2,1).
        boolean shapeOK = (absRowDiff == 2 && absColDiff == 1) || (absRowDiff == 1 && absColDiff == 2); // check L shape
        if (!shapeOK) {
            return false; // not a horse move
        }

        // (horse leg cannot be blocked)
        int legRow;  // position of the blocking square
        int legCol;

        if (absRowDiff == 2) { // moving mainly in row direction
            legRow = currentRow + (rowDiff > 0 ? 1 : -1); // one step in row direction
            legCol = currentCol;                          // same column
        }
        else { // absColDiff == 2, moving mainly in column direction
            legRow = currentRow;                          // same row
            legCol = currentCol + (colDiff > 0 ? 1 : -1); // one step in col direction
        }

        if (model.getPieceAt(legRow, legCol) != null) {
            return false; // horse leg blocked
        }

        return true; // legal horse move
    }
}

