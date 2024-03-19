package com.github.davenedde.gameengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Board {
    static final int ROWS = 3;
    static final int COLS = 3;

    private Marker[][] spaces = new Marker[ROWS][COLS];
    private Player currentPlayer;


    public Board(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        for (int row = 0; row < spaces.length; row++) {
            for (int col = 0; col < spaces[row].length; col++) {
                spaces[row][col] = Marker.EMPTY;
            }
        }
    }

    private Board(Board sourceBoard) {
        this.currentPlayer = sourceBoard.currentPlayer;
        IntStream.range(0, ROWS).forEach(row -> {
                IntStream.range(0, COLS).forEach(col -> {
                        spaces[row][col] = sourceBoard.getMarker(row, col);
                    });
            });
    }

    private Board(Player currentPlayer, Marker[][] spaces) {
        this.currentPlayer = currentPlayer;
        this.spaces = spaces;
    }

    private String toString = null;

    public String toString() {
        if (toString == null) {
            toString = "Current com.github.davenedde.gameengine.Player: " + currentPlayer + "\n" +
                IntStream.range(0, ROWS).mapToObj(row -> {
                    return IntStream.range(0, COLS).mapToObj(col -> {
                            switch (spaces[row][col]) {
                                case X: return "X";
                                case O: return "O";
                                case EMPTY: return Integer.toString(new Position(row, col).toPositionNumber());
                                default: throw new RuntimeException("fail");
                            }
                        })
                        .collect(Collectors.joining("|"));
                })
                .collect(Collectors.joining("\n-|-|-\n"));
        }

        return toString;
    }


    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOtherPlayer() {
        return currentPlayer.getOtherPlayer();
    }


    /**
     * Return a list of boards that are functionally equivilent to this board
     * through reflection and rotation
     */
    public List<Board> getEquivilentBoards() {
        Board rotated90 = this.rotate90DegreesClockwise();
        Board rotated180 = rotated90.rotate90DegreesClockwise();
        Board rotated270 = rotated180.rotate90DegreesClockwise();
        return Stream.of(
            rotated90,
            rotated90.reflectXAxis(),
            rotated90.reflectYAxis(),
            rotated180,
            rotated180.reflectXAxis(),
            rotated180.reflectYAxis(),
            rotated270,
            rotated270.reflectXAxis(),
            rotated270.reflectYAxis())
            .distinct()
            .collect(Collectors.toList());
    }

    /** Return a new Board that has been rotated 90 degrees clockwise */
    private Board rotate90DegreesClockwise() {
        int rows = spaces.length;
        int cols = spaces[0].length;
        Marker[][] rotatedSpaces = new Marker[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotatedSpaces[col][rows - 1 - row] = spaces[row][col];
            }
        }

        return new Board(currentPlayer, rotatedSpaces);
    }

    /** Return a new Board that has been relected on the Y axis */
    private Board reflectYAxis() {
        int rows = spaces.length;
        int cols = spaces[0].length;
        Marker[][] reflectedSpaces = new Marker[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                reflectedSpaces[row][cols - 1 - col] = spaces[row][col];
            }
        }

        return new Board(currentPlayer, reflectedSpaces);
    }

    /** Return a new Board that has been relected on the X axis */
    private Board reflectXAxis() {
        int rows = spaces.length;
        int cols = spaces[0].length;
        Marker[][] reflectedSpaces = new Marker[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                reflectedSpaces[rows - 1 - row][col] = spaces[row][col];
            }
        }

        return new Board(currentPlayer, reflectedSpaces);
    }

    public Board playMove(Position pos) {
        Board nextBoard = new Board(this);
        nextBoard.setMarker(pos.getRow(), pos.getCol(), currentPlayer.getMarker());

        // Assume two player game that alternates
        nextBoard.currentPlayer = currentPlayer.getOtherPlayer();

        return nextBoard;
    }


    public Optional<Player> getWinner() {
        for (int row = 0; row < ROWS; row++) {
            final int finalRow = row;
            List<Marker> rowValues = IntStream.range(0, COLS)
                    .mapToObj(col -> getMarker(finalRow, col))
                    .distinct()
                    .collect(Collectors.toList());
            if (rowValues.size() == 1 && !rowValues.get(0).equals(Marker.EMPTY)) {
                return Optional.of(Player.getPlayer(rowValues.get(0)));
            }
        }

        for (int col = 0; col < COLS; col++) {
            final int finalCol = col;
            List<Marker> colValues = IntStream.range(0, ROWS)
                    .mapToObj(row -> getMarker(row, finalCol))
                    .distinct()
                    .collect(Collectors.toList());
            if (colValues.size() == 1 && !colValues.get(0).equals(Marker.EMPTY)) {
                return Optional.of(Player.getPlayer(colValues.get(0)));
            }
        }

        List<Marker> diagValues1 = IntStream.range(0, ROWS)
            .mapToObj(row -> getMarker(row, row))
            .distinct()
            .collect(Collectors.toList());
        if (diagValues1.size() == 1 && !diagValues1.get(0).equals(Marker.EMPTY)) {
            return Optional.of(Player.getPlayer(diagValues1.get(0)));
        }

        List<Marker> diagValues2 = IntStream.range(0, ROWS)
            .mapToObj(row -> getMarker(row, ROWS - row - 1))
            .distinct()
            .collect(Collectors.toList());
        if (diagValues2.size() == 1 && !diagValues2.get(0).equals(Marker.EMPTY)) {
            return Optional.of(Player.getPlayer(diagValues2.get(0)));
        }


        if (Position.stream().anyMatch(position -> getMarker(position.getRow(), position.getCol()) == Marker.EMPTY)) {
            return Optional.empty();
        } else {
            // No winning moves and no empty spaces means this is a tie
            return Optional.of(Player.TIE);
        }
    }


    private void setMarker(int row, int col, Marker marker) {
        if (spaces[row][col] == Marker.EMPTY) {
            spaces[row][col] = marker;
        } else {
            throw new RuntimeException("Tried to put piece in non-empty space");
        }
    }

    public List<Position> getEmptyPositions() {
        List<Position> emptyPositions = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (spaces[row][col] == Marker.EMPTY) {
                    emptyPositions.add(new Position(row, col));
                }
            }
        }

        return emptyPositions;
    }

    public Marker getMarker(int row, int col) {
        return spaces[row][col];
    }

    public int hashCode() {
        final int PRIME = 31; // Prime number used as a multiplier
        int result = 1;

        for (int row = 0; row < spaces.length; row++) {
            for (int col = 0; col < spaces[row].length; col++) {
                result = PRIME * result + spaces[row][col].ordinal();
            }
        }

        return PRIME * result + currentPlayer.ordinal();
    }

    public boolean equals(Object obj) {
        if (obj instanceof Board) {
            return currentPlayer.equals(((Board)obj).currentPlayer) &&
                Arrays.deepEquals(spaces, ((Board)obj).spaces);
        } else {
            return false;
        }
    }
}