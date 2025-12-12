package edu.sustech.xiangqi.model;

/**
 * 车
 */
public class ChariotPiece extends AbstractPiece {

    public ChariotPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed); // init basic data
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();   // current row
        int currentCol = getCol();   // current column

        if (currentRow == targetRow && currentCol == targetCol) {
            return false; // no move
        }

        // 车只能直走 (same row or same column)
        boolean sameRow = currentRow == targetRow;       // horizontal move
        boolean sameCol = currentCol == targetCol;       // vertical move
        if (!sameRow && !sameCol) {
            return false;          // diagonal/other is illegal
        }

        // 检查中间有没有棋子 (no pieces in between)
        if (sameRow) { // horizontal move
            int start = Math.min(currentCol, targetCol) + 1; // first middle col
            int end = Math.max(currentCol, targetCol);       // last exclusive

            for (int c = start; c < end; c++) {              // traverse columns between
                if (model.getPieceAt(currentRow, c) != null) {
                    return false; // blocked
                }
            }
        } else { // sameCol, vertical move
            int start = Math.min(currentRow, targetRow) + 1; // first middle row
            int end = Math.max(currentRow, targetRow);       // last exclusive

            for (int r = start; r < end; r++) {              // traverse rows between
                if (model.getPieceAt(r, currentCol) != null) {
                    return false; // blocked
                }
            }
        }

        return true; // path clear, geometry OK
    }
}

