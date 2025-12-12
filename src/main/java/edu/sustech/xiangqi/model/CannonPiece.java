package edu.sustech.xiangqi.model;

/**
 * 炮
 */
public class CannonPiece extends AbstractPiece {

    public CannonPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed); // use base constructor
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();   // current row
        int currentCol = getCol();   // current column

        if (currentRow == targetRow && currentCol == targetCol) {
            return false; // no move
        }

        boolean sameRow = currentRow == targetRow;       // straight horizontally?
        boolean sameCol = currentCol == targetCol;       // straight vertically?
        if (!sameRow && !sameCol) {
            return false;          // cannon must move straight
        }

        // Count pieces between current position and target
        int betweenCount = 0;                            // number of pieces between

        if (sameRow) { // horizontal move
            int start = Math.min(currentCol, targetCol) + 1; // first middle col
            int end = Math.max(currentCol, targetCol);       // last exclusive

            for (int c = start; c < end; c++) {              // check each square between
                if (model.getPieceAt(currentRow, c) != null) {
                    betweenCount++; // found a screen
                }
            }
        }
        else { // sameCol (vertical move)
            int start = Math.min(currentRow, targetRow) + 1; // first middle row
            int end = Math.max(currentRow, targetRow);       // last exclusive

            for (int r = start; r < end; r++) {              // check each square between
                if (model.getPieceAt(r, currentCol) != null) betweenCount++; // found a screen
            }
        }

        AbstractPiece targetPiece = model.getPieceAt(targetRow, targetCol); // piece at destination (may be null)

        // 炮不吃子时不能隔山 (non-capture move: no pieces in between)
        if (targetPiece == null) {
            return betweenCount == 0; // must have clear path
        }

        // 炮吃子时必须隔一个子 (capture move: exactly one piece between)
        // Note: we don't check color here; ChessBoardModel should prevent capturing own piece
        return betweenCount == 1; // legal capture if exactly one screen
    }
}
