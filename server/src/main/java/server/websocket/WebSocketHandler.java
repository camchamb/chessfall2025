package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import data.*;
import dataaccess.*;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final UserDAO userAccess;
    private final GameDAO gameAccess;
    private final AuthDAO authAccess;


    public WebSocketHandler(UserDAO userAccess, GameDAO gameAccess, AuthDAO authAccess) {
        this.userAccess = userAccess;
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.getAuthToken(), action.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(new Gson().fromJson(ctx.message(), UserGameCommand.class), ctx.session);
                case LEAVE -> leave(action.getAuthToken(), action.getGameID(), ctx.session);
                case RESIGN -> resign(action.getAuthToken(), action.getGameID(), ctx.session);
                default -> connections.messageRoot(ctx.session,
                        new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Command"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, int gameID, Session session) throws IOException {
        connections.add(authToken, gameID, session);
        String message;
        GameData gameData;
        try {
            var authData = authAccess.getAuth(authToken);
            if (authData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
                connections.messageRoot(session, error);
                return;
            }
            gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "No Such Game");
                connections.messageRoot(session, error);
                return;
            }
            if (authData.username().equals(gameData.whiteUsername())) {
                message = String.format("%s connected as White Player", authData.username());
            } else if (authData.username().equals(gameData.blackUsername())) {
                message = String.format("%s connected as Black Player", authData.username());
            } else {
                message = String.format("%s connected as an observer", authData.username());
            }
        } catch (DataAccessException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
            connections.messageRoot(session, error);
            return;
        }
        var loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        connections.messageRoot(session, loadGame);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, gameID, notification);
    }

    private void leave(String authToken, int gameID, Session session) throws IOException {
        connections.remove(authToken);
        String message;
        GameData gameData;
        AuthData authData;
        try {
            authData = authAccess.getAuth(authToken);
            message = String.format("%s left the game", authData.username());
            gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "No Such Game");
                connections.messageRoot(session, error);
                return;
            }
            if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(authData.username())) {
                gameAccess.updateGame(new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game()));
            }
            if (gameData.blackUsername() != null &&gameData.blackUsername().equals(authData.username())) {
                gameAccess.updateGame(new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game()));
            }
        } catch (DataAccessException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
            connections.messageRoot(session, error);
            return;
        }
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, gameID, notification);
    }

    private void makeMove(UserGameCommand moveCommand, Session session) throws IOException {
        ChessMove move = moveCommand.getMove();
        String message;
        String username;
        GameData gameData;
        ChessGame.TeamColor color = null;
        ChessGame game;
        try {
            var authData = authAccess.getAuth(moveCommand.getAuthToken());
            if (authData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
                connections.messageRoot(session, error);
                return;
            }
            gameData = gameAccess.getGame(moveCommand.getGameID());
            if (gameData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "No Such Game");
                connections.messageRoot(session, error);
                return;
            }
            username = authData.username();
            game = gameData.game();
            if (game.gameOver) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game is Finished");
                connections.messageRoot(session, error);
                return;
            }
            if (username.equals(gameData.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            } if (username.equals(gameData.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }
        } catch (DataAccessException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
            connections.messageRoot(session, error);
            return;
        }
        try {
            if (!game.getTeamTurn().equals(color)) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Can't Make a Move");
                connections.messageRoot(session, error);
                return;
            }
            game.makeMove(move);
            gameAccess.updateGame(gameData);
            message = String.format("%s moved %s to %s", username, move.getStartPosition(), move.getEndPosition());
            var loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
            connections.messageRoot(session, loadGame);
            connections.broadcast(moveCommand.getAuthToken(), moveCommand.getGameID(), loadGame);
            String checkMessage = checkMessages(game, gameData);
            if (checkMessage != null) {
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, checkMessage);
                connections.messageRoot(session, notification);
                connections.broadcast(moveCommand.getAuthToken(), moveCommand.getGameID(), notification);
            }
        } catch (DataAccessException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid Move");
            connections.messageRoot(session, error);
            return;
        } catch (InvalidMoveException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            connections.messageRoot(session, error);
            return;
        }
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(moveCommand.getAuthToken(), moveCommand.getGameID(), notification);
    }

    private String checkMessages(ChessGame game, GameData gameData) {
        if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            return "Game ends in Stalemate";
        }
        if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            return "Game ends in Stalemate";
        }
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            return String.format("%s is in checkmate", gameData.whiteUsername());
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            return String.format("%s is in checkmate", gameData.blackUsername());
        }
        if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            return String.format("%s is in check", gameData.whiteUsername());
        }
        if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            return String.format("%s is in check", gameData.blackUsername());
        }
        return null;
    }


    private void resign(String authToken, int gameID, Session session) throws IOException {
        String message;
        GameData gameData;
        AuthData authData;
        try {
            authData = authAccess.getAuth(authToken);
            message = String.format("%s resigned the game", authData.username());
            gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "No Such Game");
                connections.messageRoot(session, error);
                return;
            }
            if (gameData.game().gameOver) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Game already over");
                connections.messageRoot(session, error);
                return;
            }
            if (!authData.username().equals(gameData.whiteUsername()) && !authData.username().equals(gameData.blackUsername())) {
                var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Can't resign");
                connections.messageRoot(session, error);
                return;
            }
            gameData.game().gameOver = true;
            gameAccess.updateGame(gameData);
            var loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
//            connections.messageRoot(session, loadGame);
//            connections.broadcast(authToken, gameID, loadGame);
        } catch (DataAccessException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Bad Request");
            connections.messageRoot(session, error);
            return;
        }
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, gameID, notification);
        connections.messageRoot(session, notification);
    }
}