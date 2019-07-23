package com.github.davenedde.gameengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Board {
    static final int ROWS = 3;
    static final int COLS = 3;

    private Marker[][] spaces = new Marker[ROWS][COLS];
    private Player currentPlayer;


    public Board(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        IntStream.range(0, ROWS).forEach(row -> {
                IntStream.range(0, COLS).forEach(col -> {
                        spaces[row][col] = Marker.EMPTY;
                    });
            });
    }

    private Board(Board sourceBoard) {
        this.currentPlayer = sourceBoard.currentPlayer;
        IntStream.range(0, ROWS).forEach(row -> {
                IntStream.range(0, COLS).forEach(col -> {
                        spaces[row][col] = sourceBoard.getMarker(row, col);
                    });
            });
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
        return toString().hashCode();
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