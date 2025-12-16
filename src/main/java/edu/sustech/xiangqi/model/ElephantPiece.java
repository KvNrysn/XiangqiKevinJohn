package edu.sustech.xiangqi.model;

public class ElephantPiece extends AbstractPiece {

    public ElephantPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();

        if (currentRow == targetRow && currentCol == targetCol) {
            return false; // no move
        }
        //diagonal movement rule
        int rowDiff = targetRow - currentRow;
        int colDiff = targetCol - currentCol;
        int absRowDiff = Math.abs(rowDiff);
        int absColDiff = Math.abs(colDiff);
        if (!(absRowDiff == 2 && absColDiff == 2)) {
            return false;
        }

        //River restriction
        if (isRed()) {
            if (targetRow < 5) {
                return false;
            }
        }
        else {  //black side
            if (targetRow > 4) {
                return false;
            }
        }

        // eye rule, mid square
        int midRow = (currentRow + targetRow) / 2;
        int midCol = (currentCol + targetCol) / 2;

        if (model.getPieceAt(midRow, midCol) != null) {
            return false;
        }
        return true;
    }
}
