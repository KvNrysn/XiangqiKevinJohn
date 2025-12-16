package edu.sustech.xiangqi.model;

public class HorsePiece extends AbstractPiece {
    public HorsePiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();

        if (currentRow == targetRow && currentCol == targetCol) {
            return false;
        }

        int rowDiff = targetRow - currentRow;
        int colDiff = targetCol - currentCol;
        int absRowDiff = Math.abs(rowDiff);
        int absColDiff = Math.abs(colDiff);


        boolean shapeOK =
                (absRowDiff == 2 && absColDiff == 1) || (absRowDiff == 1 && absColDiff == 2);
        if (!shapeOK) {
            return false;
        }

        // horse leg
        int legRow;
        int legCol;
        if (absRowDiff == 2) {  //mostly moving vertically
            legRow = currentRow + (rowDiff > 0 ? 1 : -1); // one step in row direction, came column
            legCol = currentCol;
        }
        else {
            legRow = currentRow;                          // same row
            legCol = currentCol + (colDiff > 0 ? 1 : -1); // one step in col direction
        }
        if (model.getPieceAt(legRow, legCol) != null) {
            return false;
        }
        return true;
    }
}

