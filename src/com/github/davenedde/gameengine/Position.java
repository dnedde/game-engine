package com.github.davenedde.gameengine;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/** A position on the board */
class Position implements Comparable<Position> {
    private int row;
    private int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    @Override
    public String toString() {
        //return "[" + row + "," + col + "], posNum=" + 
        return Integer.toString(toPositionNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return toPositionNumber() == position.toPositionNumber();
    }

    public static Position fromString(String positionString) {
        Integer positionNumber = Integer.parseInt(positionString);
        return new Position(positionToRow(positionNumber), positionToCol(positionNumber));
    }

    static Position fromPositionNumber(int positionNumber) {
        return new Position(positionToRow(positionNumber), positionToCol(positionNumber));
    }

    @Override
    public int hashCode() {
        return toPositionNumber();
    }

    int toPositionNumber() {
        return col + 1 + row * Board.COLS;
    }

    public static Stream<Position> stream() {
        return IntStream.range(1, Board.ROWS * Board.COLS + 1)
            .mapToObj(positionNumber -> fromPositionNumber(positionNumber));
    }


    static private int positionToRow(int positionNumber) {
        return (positionNumber - 1) / Board.COLS;
    }

    static private int positionToCol(int positionNumber) {
        return positionNumber - (positionToRow(positionNumber) * Board.COLS) - 1;
    }

    @Override
    public int compareTo(Position o) {
        return toPositionNumber() - o.toPositionNumber();
    }
}