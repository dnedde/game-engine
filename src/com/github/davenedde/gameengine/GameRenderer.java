package com.github.davenedde.gameengine;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class GameRenderer {
    static void printGame(BoardRewards boardRewards, GameResult result) {
        Board currentBoard = new Board(Player.O);

        printBoardAndWeights(boardRewards, currentBoard);

        for (Position currentMove : result.getMoves()) {
            currentBoard = currentBoard.playMove(currentMove);

            printBoardAndWeights(boardRewards, currentBoard);
        }
    }


    static class PosWeight {
        public Position getPos() {
            return pos;
        }

        public Double getWeight() {
            return weight;
        }

        Position pos;
        Double weight;

        public PosWeight(Position pos, Double weight) {
            this.pos = pos;
            this.weight = weight;
        }

    }

    // print the board when it is the currentPlayer's turn to go along with weights for the various moves for the current player
    static void printBoardAndWeights(BoardRewards boardRewards, Board board) {
        System.out.println("---\nStarting com.github.davenedde.gameengine.Board: " + board);

        board.getEmptyPositions().stream()
            .map(pos -> {
                    Board nextMoveBoard = board.playMove(pos);
                    double nextMoveValue = boardRewards.getRewardOtherPlayer(nextMoveBoard);
                    return new PosWeight(pos, nextMoveValue);
                })
            .sorted(Comparator
                .comparing(PosWeight::getWeight, Collections.reverseOrder())
                .thenComparing(PosWeight::getPos))
            .forEach(posWeight -> System.out.println(String.format("\tcom.github.davenedde.gameengine.Position %s weight: % f", posWeight.pos, posWeight.weight)));
    }


    static void printBestWeights(BoardRewards boardRewards) {
        Board currentBoard = new Board(Player.O);

        System.out.println("---\nStarting board: " + currentBoard);

        while (currentBoard != null && !currentBoard.getWinner().isPresent()) {

            Double maxValue = -Double.MAX_VALUE;
            Position bestMove = null;
            Board bestBoard = null;

            List<Position> emptyPositions = currentBoard.getEmptyPositions();
            for (Position emptyBoardPosition : emptyPositions) {
                Board nextBoard = currentBoard.playMove(emptyBoardPosition);
                Double nextMoveValue = boardRewards.getRewardOtherPlayer(nextBoard);

                System.out.println(String.format("\tcom.github.davenedde.gameengine.Position %s weight: % f", emptyBoardPosition, nextMoveValue));

                if (nextMoveValue > maxValue) {
                    maxValue = nextMoveValue;
                    bestBoard = nextBoard;
                    bestMove = emptyBoardPosition;
                }
            }

            currentBoard = bestBoard;

            System.out.println("Best move: " + bestMove);
            System.out.println("---\ncom.github.davenedde.gameengine.Board for: " + currentBoard);
        }
    }


    static void printFirstMoveWeights(PrintWriter out, BoardRewards boardRewards) {
        Board startingBoard = new Board(Player.O);

        out.println(
            startingBoard.getEmptyPositions().stream()
            .map(position -> boardRewards.getRewardOtherPlayer(startingBoard.playMove(position)))
            .map(reward -> String.format("%f", reward))
            .collect(Collectors.joining("\t")));
    }
}