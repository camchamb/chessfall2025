package websocket.messages;

import chess.ChessGame;
import chess.ChessMove;
import data.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    private String message = null;
    private ChessGame game = null;
    private String errorMessage = null;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        if (type.equals(ServerMessageType.ERROR)) {
            this.errorMessage = message;
        }
        else {
            this.message = message;
        }
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;
    }



    public String getMessage() {
        return message;
    }

    public ChessGame getGame() {
        return game;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
