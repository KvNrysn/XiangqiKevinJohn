package edu.sustech.xiangqi.model;

public abstract class AbstractPiece {
    private final String name;    // piece name, "General", "Soldier",etc
    private final boolean isRed;  // true = red side, false = black side
    private int row;
    private int col;

    public AbstractPiece(String name, int row, int col, boolean isRed) {  //purpose is to initialize the object's internal data
        this.name = name;
        this.row = row;
        this.col = col;
        this.isRed = isRed;
    }
    public String getName() {
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
    }   //moving current piece to a new position, rule checking is done before calling this method (in ChessBoardModel)

    public abstract boolean canMoveTo(int targetRow, int targetCol, ChessBoardModel model);
    // Check whether current piece can move to new position, this only checks the movement pattern of that piece, not full game rules
    @Override
    public String toString() {
        return (isRed ? "Red " : "Black ") + name + " at (" + row + ", " + col + ")";
    }
}
