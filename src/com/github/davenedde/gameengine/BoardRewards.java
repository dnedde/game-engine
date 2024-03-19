package com.github.davenedde.gameengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a map of board + player whose turn it is (current player that has not yet moved) to reward.
 */
class BoardRewards {
    /** Reward always matches the player whose turn it is */
    private final Map<Board,Double> boardPlayerTurnToValueMap = new HashMap<>();

    /** Map a board to equivalent boards (reflected and rotated) that would have the same value for the current player */
    private final Map<Board,List<Board>> boardToEquivilentBoardsMap = new HashMap<>();


    public void clear() {
        boardPlayerTurnToValueMap.clear();
    }

    /** Return the reward for the currentPlayer (about to play a move) of the specified board */
    public double getRewardCurrentPlayer(Board board) {
        return boardPlayerTurnToValueMap.computeIfAbsent(board, key -> 0.5);
    }

    /** Return the reward for the non-currentPlayer (just played a move) of the specified board */
    public double getRewardOtherPlayer(Board board) {
        return 1.0 - getRewardCurrentPlayer(board);
    }

    /** Store reward for player who is about to play a move.  Also store reward for the same board in other orientations */
    public void setRewardCurrentPlayer(Board board, double value) {
        boardPlayerTurnToValueMap.put(board, value);

        List<Board> equivalentBoards = boardToEquivilentBoardsMap.get(board);
        if (equivalentBoards == null) {
            equivalentBoards = board.getEquivalentBoards();
            boardToEquivilentBoardsMap.put(board, equivalentBoards);
        }

        equivalentBoards.forEach( // Store values for equivalent boards to speed training
            equivilentBoard -> boardPlayerTurnToValueMap.put(equivilentBoard, value));
    }
}