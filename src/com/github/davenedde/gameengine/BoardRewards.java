package com.github.davenedde.gameengine;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the reward that a board + player whose turn it (current player) is gives for a specific player.
 */
class BoardRewards {
    /** Reward always matches the player whose turn it is */
    private Map<Board,Double> boardPlayerTurnToValueMap = new HashMap<>();

    public void clear() {
        boardPlayerTurnToValueMap.clear();
    }

    /** Return the reward for the currentPlayer of the specified board */
    public double getRewardCurrentPlayer(Board board) {
        double rewardForCurrentPlayer = boardPlayerTurnToValueMap.computeIfAbsent(board,
            key -> Double.valueOf(0.5));

        return rewardForCurrentPlayer;
    }

    /** Return the reward for the non-currentPlayer of the specified board */
    public double getRewardOtherPlayer(Board board) {
        return 1.0 - getRewardCurrentPlayer(board);
    }

    public void setRewardCurrentPlayer(Board board, double value) {
        boardPlayerTurnToValueMap.put(board, value);
    }
}