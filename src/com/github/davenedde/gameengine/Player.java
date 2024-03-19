package com.github.davenedde.gameengine;

import java.util.Arrays;

public enum Player {
    X(Marker.X),
    O(Marker.O),
    TIE(null);


    Marker marker;

    Player(Marker marker) {
        this.marker = marker;
    }

    Player getOtherPlayer() {
        if (this == X) {
            return O;
        } else if (this == O) {
            return X;
        } else {
            throw new RuntimeException("No other player found for com.github.davenedde.gameengine.Player " + this);
        }
    }

    Marker getMarker() {
        return marker;
    }

    static Player getPlayer(Marker marker) {
        return Arrays.stream(Player.values())
            .filter(player -> marker.equals(player.getMarker()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No player found for com.github.davenedde.gameengine.Marker " + marker));
    }
}
