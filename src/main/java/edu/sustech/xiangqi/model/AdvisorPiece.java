package edu.sustech.xiangqi.model;
    public class AdvisorPiece extends AbstractPiece {

        public AdvisorPiece(String name, int row, int col, boolean isRed) {
            super(name, row, col, isRed);
        }

        @Override
        public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
            int currentRow = getRow();
            int currentCol = getCol();

            if (currentRow == targetRow && currentCol == targetCol) {
                return false;
            }
            if (isRed()) {
                if (targetRow < 7 || targetRow > 9 || targetCol < 3 || targetCol > 5) {
                    return false;
                }
            }
            else {
                if (targetRow < 0 || targetRow > 2 || targetCol < 3 || targetCol > 5) {
                    return false;
                }
            }

            int rowDiff = Math.abs(targetRow - currentRow);
            int colDiff = Math.abs(targetCol - currentCol);

            return rowDiff == 1 && colDiff == 1;
        }
    }
