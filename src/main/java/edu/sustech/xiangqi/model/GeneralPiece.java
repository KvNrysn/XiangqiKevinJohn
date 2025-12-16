package edu.sustech.xiangqi.model;

public class GeneralPiece extends AbstractPiece {   //inherits variable and methods from parents
    //constructor
    public GeneralPiece(String name, int row, int col, boolean isRed) {
        super(name, row, col, isRed);       // call the parent constructor and set initial position, name, and color
    }

    @Override
    public boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model) {
        int currentRow = getRow();
        int currentCol = getCol();   // the row n column where the general is currently standing

        if (currentRow == targetRow && currentCol == targetCol) {
            return false;  // can't "move" to the same square
        }

        // Check palace boundaries
        // Red palace: rows 7-9, columns 3-5
        // Black palace: rows 0-2, columns 3-5
        if (isRed()) {
            if (targetRow < 7 || targetRow > 9 || targetCol < 3 || targetCol > 5) {
                return false; // outside palace = illegal
            }
        } else { // black general
            if (targetRow < 0 || targetRow > 2 || targetCol < 3 || targetCol > 5) {
                return false; // outside palace = illegal
            }
        }

        // Generals ability to move (orthogonally)
        int rowDiff = Math.abs(targetRow - currentRow);
        int colDiff = Math.abs(targetCol - currentCol);

        if (rowDiff + colDiff != 1) {
            return false; // if movement is more than 1 step or diagonal → illegal
        }

        //flying-general rule
          AbstractPiece otherGeneral = model.findOtherGeneral(this); // helper method CBM
        if (otherGeneral != null && targetCol == otherGeneral.getCol()) {   // If move = they are in the same column
            int start = Math.min(targetRow, otherGeneral.getRow()) + 1; //checking squares between the two
            int end = Math.max(targetRow, otherGeneral.getRow());
            boolean blocked = false;    //asssuming nothing is blocking them
            for (int r = start; r < end; r++) {     // for loop checks every row inbetween them 1by1
                if (model.getPieceAt(r, targetCol) != null) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                return false;
            }
        }
        return true; // if all checks pass, the move is legal
    }
}