package edu.sustech.xiangqi.model;

public class SoldierPiece extends AbstractPiece {

    public SoldierPiece(String name, int row, int col, boolean isRed) {
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
        int colDiff = Math.abs(targetCol - currentCol);

        if (isRed()) {  // if red cross river
            boolean crossedRiver = currentRow < 5;  //river between row 4&5

            if (!crossedRiver) {
                return rowDiff == -1 && colDiff == 0;
            }
            else {
                if (rowDiff == -1 && colDiff == 0) {
                    return true;
                }
                return rowDiff == 0 && colDiff == 1;
            }

        }
        else {  //if black cross river
            boolean crossedRiver = currentRow >= 5;
            if (!crossedRiver) {
                return rowDiff == 1 && colDiff == 0;
            }
            else {
                if (rowDiff == 1 && colDiff == 0) {
                    return true;
                }
                return rowDiff == 0 && colDiff == 1;
            }
        }
    }
}
