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
            return false;   // Cannot "move" to its same location
        }

        int rowDiff = targetRow - currentRow;
        int colDiff = Math.abs(targetCol - currentCol);

        if (isRed()) {  // Determine if the soldier has crossed the river (river between row 4&5)
            boolean crossedRiver = currentRow < 5;  // Red soldiers move upward (row decreases)

            if (!crossedRiver) {
                return rowDiff == -1 && colDiff == 0;  // Before crossing: can only move forward one step
            } else {    //after crossing
                if (rowDiff == -1 && colDiff == 0) return true; // can move forward
                return rowDiff == 0 && colDiff == 1;            // can move left or right
            }

        } else {
            boolean crossedRiver = currentRow >= 5;  // Black soldiers move downward (row increases)
            if (!crossedRiver) {
                return rowDiff == 1 && colDiff == 0;  // Before crossing: can only move forward one step
            } else {    // After crossing
                if (rowDiff == 1 && colDiff == 0) return true; // can move forward
                return rowDiff == 0 && colDiff == 1;           // can move left or right
            }
        }
    }
}
