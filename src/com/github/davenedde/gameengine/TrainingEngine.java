package com.github.davenedde.gameengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

class TrainingEngine {
    /** Rate by which board rewards are propagated back up the move tree */
    private static double TRAINING_RATE = 0.4;

    /** Starting percentage of the number of random moves that will be made */
    private static double EXPLORATORY_RATE = 1.0;

    /** Percentage that the exploratory rate will be reduced by each game played */
    private static double EXPLORATORY_DECAY_RATE = 0.999995;

    private static final Board STARTING_BOARD = new Board(Player.O);
    private static final List<Position> STARTING_POSITIONS = STARTING_BOARD.getEmptyPositions();

    long MAX_TRAINING_GAMES = 600_000;
    long MAX_O_TRAINING_GAMES = (long)(MAX_TRAINING_GAMES * 0.6);
    long MAX_X_TRAINING_GAMES = (long)(MAX_TRAINING_GAMES * 0.8);
    long MAX_BACKUP_GAMES = MAX_O_TRAINING_GAMES;

    /** Track number of random moves for logging */
    private long playerOMadeRandomMove = 0;
    private long playerXMadeRandomMove = 0;

    BoardRewards boardRewards = new BoardRewards();
    double currentExploratoryRate;
    long trainingGameIndex;

    Random random = new Random();


    public BoardRewards train() throws IOException {
        trainOnce();

        return boardRewards;
    }


    private void trainOnce() throws IOException {
        boardRewards.clear();

        long oWins = 0;
        long xWins = 0;
        long draws = 0;
        long oPerfectStreak = 0;
        long oLossesAfterTraining = 0;
        playerOMadeRandomMove = 0;
        playerXMadeRandomMove = 0;
        currentExploratoryRate = EXPLORATORY_RATE;
        double[] movingRewardSum = new double[9];

        try (PrintWriter out = new PrintWriter(Files.newOutputStream(
                    Paths.get("weights.tsv")), true)) {

            //printInitialMoveWeightHeader(out);
            System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "i", "oWins", "xWins", "draws", "oStreak", "oRand", "xRand", "ExplRate"));


            trainingGameIndex = 0;
            while (trainingGameIndex < MAX_TRAINING_GAMES) {
                GameResult gameResult = playGame();
                currentExploratoryRate *= EXPLORATORY_DECAY_RATE;

                switch (gameResult.getWinner()) {
                    case TIE: draws++; break;
                    case O: oWins++; break;
                    case X: xWins++; break;
                }

                for (int iPos = 0; iPos < STARTING_POSITIONS.size(); iPos++) {
                    movingRewardSum[iPos] += boardRewards.getRewardOtherPlayer(STARTING_BOARD.playMove(STARTING_POSITIONS.get(iPos)));
                }

                if ((trainingGameIndex+1) % 1000 == 0) {
                    out.println(Arrays.stream(movingRewardSum)
                        .mapToObj(rewardSum -> String.format("%f", rewardSum / 1000.0))
                        .collect(Collectors.joining("\t")));
                    for (int iPos = 0; iPos < STARTING_POSITIONS.size(); iPos++) {
                        movingRewardSum[iPos] = 0;
                    }
                }

                if (++trainingGameIndex % 10000 == 0) {
                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%f",
                            trainingGameIndex, oWins, xWins, draws, oPerfectStreak, playerOMadeRandomMove, playerXMadeRandomMove, currentExploratoryRate));

                    if (xWins == 0) {
                        oPerfectStreak++;
                        if (oPerfectStreak > 2) {
                            break;
                        }
                    } else {
                        oPerfectStreak = 0;
                        if (trainingGameIndex > MAX_O_TRAINING_GAMES) {
                            oLossesAfterTraining++;
                            if (oLossesAfterTraining > 5) {
                                //break;
                            }
                        }
                    }

                    oWins = 0;
                    xWins = 0;
                    draws = 0;
                    playerOMadeRandomMove = 0;
                    playerXMadeRandomMove = 0;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed when writing stats", e);
        }
    }

    private GameResult playGame() {
        Board board = new Board(Math.random() > 0.5 ? Player.O : Player.X);

        return playNextMove(board);
    }


    private GameResult playNextMove(Board currentBoard) {
        return playNextMove(currentBoard, new ArrayList<>());
    }


    private GameResult playNextMove(Board currentBoard, ArrayList<Position> moves) {
        Optional<Player> winner = currentBoard.getWinner();

        if (!winner.isPresent()) {
            Optional<Position> nextMove = getNextMove(currentBoard);

            if (nextMove.isPresent()) {
                moves.add(nextMove.get());

                Board nextBoard = currentBoard.playMove(nextMove.get());

                GameResult result = playNextMove(nextBoard, moves); // Recurse

                if (trainingGameIndex < MAX_BACKUP_GAMES) {
                    backUpValue(currentBoard, nextBoard);
                }

                return result;
            } else {
                throw new RuntimeException("unexpected");
            }
        } else {
            // We have a winner
            final double reward;
            if (winner.get().equals(Player.TIE)) {
                reward = 0.5;
            } else if (winner.get().equals(currentBoard.getCurrentPlayer())) {
                reward = 1.0;
            } else if (winner.get().equals(currentBoard.getOtherPlayer())) {
                reward = 0.0;
            } else {
                throw new RuntimeException();
            }

            boardRewards.setRewardCurrentPlayer(currentBoard, reward);

            return new GameResult(winner.get(), moves, currentBoard);
        }
    }


    private void backUpValue(Board currentBoard, Board nextBoard) {
        double currentBoardValueToCurrentPlayer = boardRewards.getRewardCurrentPlayer(currentBoard);
        double nextBoardValueToCurrentPlayer = boardRewards.getRewardOtherPlayer(nextBoard);

        double newCurrentBoardValueToCurrentPlayer = currentBoardValueToCurrentPlayer +
            TRAINING_RATE * (nextBoardValueToCurrentPlayer - currentBoardValueToCurrentPlayer);

        boardRewards.setRewardCurrentPlayer(currentBoard, newCurrentBoardValueToCurrentPlayer);
    }

    /** Will return a potentially random move using existing move weights. */
    private Optional<Position> getNextMove(Board board) {
        List<Position> nextMoves = getNextMoves(board);
        if (nextMoves.size() > 0) {

            boolean explore = random.nextDouble() < currentExploratoryRate;

            Player currentPlayer = board.getCurrentPlayer();

            if (explore &&
                (currentPlayer.equals(Player.O) && trainingGameIndex < MAX_O_TRAINING_GAMES  ||
                    currentPlayer.equals(Player.X) && trainingGameIndex < MAX_X_TRAINING_GAMES)) {

                // X and O will stop making exploratory random moves after some time
                // In the endgame, X will make totally random moves

                if (currentPlayer.equals(Player.O)) {
                    playerOMadeRandomMove++;
                } else {
                    playerXMadeRandomMove++;
                }

                // Return a random, exploratory value
                return Optional.of(nextMoves.get(random.nextInt(nextMoves.size())));
            } else {
                // Return max value

                List<Double> nextMoveWeights = getNextMoveExistingRewards(board, nextMoves);

                Position maxPosition = null;
                double maxValue = -Double.MAX_VALUE;
                for (int moveIndex = 0; moveIndex < nextMoveWeights.size(); moveIndex++) {
                    if (nextMoveWeights.get(moveIndex) > maxValue) {
                        maxValue = nextMoveWeights.get(moveIndex);
                        maxPosition = nextMoves.get(moveIndex);
                    }
                }

                return Optional.ofNullable(maxPosition);
            }
        } else {
            return Optional.empty();
        }
    }

    private List<Position> getNextMoves(Board board) {
        return board.getEmptyPositions();
    }

    /**
     * Return rewards for the current player for each move.
     */
    private List<Double> getNextMoveExistingRewards(Board currentBoard, List<Position> nextMoves) {
        return nextMoves.stream()
            .map(currentBoard::playMove)
            .mapToDouble(boardAfterMove -> boardRewards.getRewardOtherPlayer(boardAfterMove))
            .boxed()
            .collect(Collectors.toList());
    }

    /**
     * SoftMax weighting maps win probabilities into good values to use for determine which next move to make.
     * We don't want to just choose the best value or the values based on the win percentages.
     * Returned values will sum to 1.0.
     * https://en.wikipedia.org/wiki/Softmax_function
     */
    public List<Double> getNextMoveSoftMaxWeights(Board currentBoard, List<Position> nextMoves, Player currentPlayer) {
        // SoftMax is the exp() of each current weight value...
        List<Double> nextMoveExpWeights = getNextMoveExistingRewards(currentBoard, nextMoves).stream()
                .mapToDouble(Math::exp)
                .boxed()
                .collect(Collectors.toList());

        double sumOfExpValues = nextMoveExpWeights.stream().mapToDouble(Double::doubleValue).sum();

        // ... divided by the sum of all exp values
        return nextMoveExpWeights.stream()
            .map(nextMoveWeight -> nextMoveWeight / sumOfExpValues)
            .collect(Collectors.toList());
    }

}