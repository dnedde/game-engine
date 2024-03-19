package com.github.davenedde.gameengine;

import java.util.HashMap;
import java.util.Map;

/** A player or game result */
public enum Player {
    X(Marker.X),
    O(Marker.O),
    TIE(null);


    private final Marker marker;

    private static final Map<Marker, Player> markerToPlayerMap = new HashMap<>();

    static {
        for (Player player : Player.values()) {
            if (player.getMarker() != null) {
                markerToPlayerMap.put(player.getMarker(), player);
            }
        }
    }


    Player(Marker marker) {
        this.marker = marker;
    }

    Player getOtherPlayer() {
        if (this == X) {
            return O;
        } else if (this == O) {
            return X;
        } else {
            throw new RuntimeException("No other player found for Player " + this);
        }
    }

    Marker getMarker() {
        return marker;
    }

    static Player getPlayer(Marker marker) {
        Player player = markerToPlayerMap.get(marker);
        if (player != null) {
            return player;
        } else {
            throw new RuntimeException("No player found for Marker " + marker);
        }
    }
}
