package com.github.davenedde.gameengine;

import java.util.List;

class GameResult {
    private Player winner;
    private List<Position> moves;
    private Board finalBoard;


    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public List<Position> getMoves() {
        return moves;
    }

    public void setMoves(List<Position> moves) {
        this.moves = moves;
    }

    public Board getFinalBoard() {
        return finalBoard;
    }

    public void setFinalBoard(Board finalBoard) {
        this.finalBoard = finalBoard;
    }

    public GameResult(Player winner, List<Position> moves, Board finalBoard) {
        this.winner = winner;
        this.moves = moves;
        this.finalBoard = finalBoard;
    }
}