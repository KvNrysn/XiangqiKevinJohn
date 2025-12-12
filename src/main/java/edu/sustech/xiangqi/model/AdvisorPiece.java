package edu.sustech.xiangqi.model;
    /**
     * 士 / 仕
     */
    public class AdvisorPiece extends AbstractPiece {

        public AdvisorPiece(String name, int row, int col, boolean isRed) {
            super(name, row, col, isRed); // pass basic info to AbstractPiece
        }

        @Override
        public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
            int currentRow = getRow();   // current row
            int currentCol = getCol();   // current column

            if (currentRow == targetRow && currentCol == targetCol) {
                return false; // can't stay in place
            }

            // palace only
            // Red palace: rows 7-9, cols 3-5
            // Black palace: rows 0-2, cols 3-5
            if (isRed()) { // red side
                if (targetRow < 7 || targetRow > 9 || targetCol < 3 || targetCol > 5) {
                    return false; // outside palace
                }
            } else { // black side
                if (targetRow < 0 || targetRow > 2 || targetCol < 3 || targetCol > 5) {
                    return false; // outside palace
                }
            }

            int rowDiff = Math.abs(targetRow - currentRow); // row distance
            int colDiff = Math.abs(targetCol - currentCol); // col distance

            // (move 1 step diagonally)
            return rowDiff == 1 && colDiff == 1; // true if exactly one-step diagonal
        }
    }
