package edu.sustech.xiangqi.model;

public abstract class AbstractPiece { //fields
    private final String name;
    private final boolean isRed;
    private int row;
    private int col;

    public AbstractPiece(String name, int row, int col, boolean isRed) {  //to initialize the object's internal data
        this.name = name;
        this.row = row;
        this.col = col;
        this.isRed = isRed;
    }// abstract bcs udk wht to do w it, only that its qualities is inherited by a subclass
    // it is a constructor method, purpose is to initialize a classes data

    public String getName() {   // getters: get current value and setters: setters update value
        return name;
    }
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getCol() {
        return col;
    }
    public void setCol(int col) {
        this.col = col;
    }
    public boolean isRed() {
        return isRed;
    }

    public void moveTo(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }   //moving current piece to a new position, storing in data

    public abstract boolean canMoveTo(      //Abstract because every piece moves differently
            int targetRow,
            int targetCol,
            ChessBoardModel model
    );      //polymorphism:  later in chessboardmodel "piece.canMoveTo(r, c, this);" piece can be any piece

    @Override
    public String toString() {
        return (isRed ? "Red " : "Black ") + name + " at (" + row + ", " + col + ")";
    }
}
