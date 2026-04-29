package data;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    @Override
    public String toString() {
        String white = (whiteUsername != null) ? whiteUsername : "Open";
        String black = (blackUsername != null) ? blackUsername : "Open";
        return gameName + "\n" +
                "White Player: " + white + "\n" +
                "Black Player: " + black + "\n";
    }
}
