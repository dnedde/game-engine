package com.github.davenedde.gameengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a map of board + player whose turn it (current player) to reward.
 */
class BoardRewards {
    /** Reward always matches the player whose turn it is */
    private Map<Board,Double> boardPlayerTurnToValueMap = new HashMap<>();

    private Map<Board,List<Board>> boardToEquivilentBoardsMap = new HashMap<>();


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

    /** Store reward for player.  Also store reward for the same board in other orientations */
    public void setRewardCurrentPlayer(Board board, double value) {
        boardPlayerTurnToValueMap.put(board, value);

        List<Board> equivilentBoards = boardToEquivilentBoardsMap.get(board);
        if (equivilentBoards == null) {
            equivilentBoards = board.getEquivilentBoards();
            boardToEquivilentBoardsMap.put(board, equivilentBoards);
        }

        equivilentBoards.forEach( // Store values for equivilent boards to speed training
            equivilentBoard -> boardPlayerTurnToValueMap.put(equivilentBoard, value));
    }
}