package edu.sustech.xiangqi.model;

public class CannonPiece extends AbstractPiece {

    public CannonPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();

        if (currentRow == targetRow && currentCol == targetCol) {
            return false;
        }
        //straight line rule same as chariot
        boolean sameRow = currentRow == targetRow;
        boolean sameCol = currentCol == targetCol;
        if (!sameRow && !sameCol) {
            return false;          // cannon must move straight
        }

        // Count pieces between current position and target
        int betweenCount = 0;                            // number of pieces between

        if (sameRow) {
            int start = Math.min(currentCol, targetCol) + 1;
            int end = Math.max(currentCol, targetCol);
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
        // capture or no capture
        AbstractPiece targetPiece = model.getPieceAt(targetRow, targetCol);
        if (targetPiece == null) {
            return betweenCount == 0;
        }   // no pcs, cannon just moving

        return betweenCount == 1; // 1 pcs in between, capturing
    }
}
