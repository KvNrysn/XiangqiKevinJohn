package edu.sustech.xiangqi.model;

public class ChariotPiece extends AbstractPiece {

    public ChariotPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);
    }
    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();
        if (currentRow == targetRow && currentCol == targetCol) {
            return false;
        }

        boolean sameRow = currentRow == targetRow;
        boolean sameCol = currentCol == targetCol;
        if (!sameRow && !sameCol) {
            return false;          // diagonal/other is illegal
        }
        //check blocking pieces (horizontal)
        if (sameRow) {
            int start = Math.min(currentCol, targetCol) + 1;    //first square after the Chariot
            int end = Math.max(currentCol, targetCol);  //stop before the target square

            for (int c = start; c < end; c++) {              //for loops check pcs
                if (model.getPieceAt(currentRow, c) != null) {
                    return false; // blocked
                }
            }
        }
        else {  //check pcs vertical
            int start = Math.min(currentRow, targetRow) + 1;
            int end = Math.max(currentRow, targetRow);

            for (int r = start; r < end; r++) {
                if (model.getPieceAt(r, currentCol) != null) {
                    return false;
                }
            }
        }
        return true;
    }
}

