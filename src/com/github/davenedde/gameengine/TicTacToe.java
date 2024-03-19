package com.github.davenedde.gameengine;/*
JAVA_HOME=$(/usr/libexec/java_home)
$JAVA_HOME/bin/javac com.github.davenedde.gameengine.TicTacToe.java
$JAVA_HOME/bin/java com.github.davenedde.gameengine.TicTacToe
*/


import java.io.IOException;
import java.util.Scanner;

public class TicTacToe {
    //private static final int TRAIN_COUNT = 500_000;
    
    public static void main(String[] args) throws IOException {
        System.out.println("Tic Tac Toe\n");

        TrainingEngine trainingEngine = new TrainingEngine();
        BoardRewards boardRewards = trainingEngine.train();

        Scanner stdInScanner = new Scanner(System.in);

        while (true) {
            Player humanPlayer = getHumanPlayer(stdInScanner);
            interact(stdInScanner, boardRewards, new Board(Player.X), humanPlayer);
        }
    }
    

    private static Player getHumanPlayer(Scanner stdInScanner) {
        System.out.print("Do you want to play X or O (X goes first): ");

        Player selectedPlayer = null;

        while (selectedPlayer == null) {
            String playerString = stdInScanner.nextLine().trim();

            if (playerString.equalsIgnoreCase("q")) {
                System.out.println("BYE!");
                System.exit(0);
            }
            
            try {
                selectedPlayer = Player.valueOf(playerString.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("com.github.davenedde.gameengine.Player must be 'X' or 'o'.  Did not recognize string " + playerString);
            }
        }
            

        return selectedPlayer;
    }


    private static void interact(Scanner stdInScanner, BoardRewards boardRewards, Board startingBoard, Player humanPlayer) {
        //boolean running = true;
        Board currentBoard = startingBoard;
        //Player computerPlayer = Player.O;

        while (!currentBoard.getWinner().isPresent()) {
            System.out.println("\n\nCurrent game: " + currentBoard);

            final Position move;

            if (currentBoard.getCurrentPlayer().equals(humanPlayer)) {
                move = getPlayerMove(stdInScanner, currentBoard);
            } else {
                move = getBestMove(boardRewards, currentBoard);

                System.out.println("\n\nComputer chooses move: " + move);
            }

            currentBoard = currentBoard.playMove(move);

            double reward = boardRewards.getRewardOtherPlayer(currentBoard);

            System.out.println(String.format("com.github.davenedde.gameengine.Player %s's move of %s gives a reward of %.2f",
                    currentBoard.getCurrentPlayer().getOtherPlayer(), move, reward));
        }

        System.out.println("\n\nGame is over!  Winner is: " + currentBoard.getWinner().get());
        System.out.println(currentBoard);
    }


    private static Position getPlayerMove(Scanner stdInScanner, Board currentBoard) {

        Position playerMove = null;

        while (playerMove == null) {
            System.out.print("\n\nEnter number on board to place piece " + currentBoard.getCurrentPlayer() + " or 'q' to quit: ");

            String idString = stdInScanner.nextLine().trim();

            if (idString.toLowerCase().startsWith("q")) {
                System.out.println("\n\nQuitting game");
                System.exit(0);
            }

            Position move = Position.fromString(idString);

            if (currentBoard.getEmptyPositions().contains(move)) {
                playerMove = move;
            } else {
                System.out.print("Invalid move " + move + ".  Try again!");
            }
        }

        return playerMove;
    }


private static Position getBestMove(BoardRewards boardRewards, Board board) {
        Double maxValue = -Double.MAX_VALUE;
        Position bestMove = null;

        for (Position emptyPosition : board.getEmptyPositions()) {
            Board nextBoard = board.playMove(emptyPosition);
            Double nextMoveValue = boardRewards.getRewardOtherPlayer(nextBoard);

            if (nextMoveValue > maxValue) {
                maxValue = nextMoveValue;
                bestMove = emptyPosition;
            }
        }

        return bestMove;
    }
}