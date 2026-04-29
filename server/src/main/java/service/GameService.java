package service;

import dataaccess.*;
import data.GameData;
import requests.*;


public class GameService {
    private final GameDAO gameAccess;
    private final AuthDAO authAccess;

    public GameService(GameDAO gameAccess, AuthDAO authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    public void clear() throws DataAccessException {
        gameAccess.clear();
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException{
        if (listGamesRequest.authToken() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        var authData = authAccess.getAuth(listGamesRequest.authToken());
        if (authData == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        var data = gameAccess.listGames();
        return new ListGamesResult(data);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException{
        if (createGameRequest.authToken() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        if (createGameRequest.gameName() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        var authData = authAccess.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        var gameID = gameAccess.createGame(createGameRequest.gameName());
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException{
        if (joinGameRequest.playerColor() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        if (joinGameRequest.authToken() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        if (!joinGameRequest.playerColor().equals("WHITE") && !joinGameRequest.playerColor().equals("BLACK")) {
            throw new DataAccessException(400, "Error: invalid color");
        }
        var authData = authAccess.getAuth(joinGameRequest.authToken());
        if (authData == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        String username = authData.username();
        GameData gameData = gameAccess.getGame(joinGameRequest.gameID());
        if (gameData == null) {
            throw new DataAccessException(400, "Error: no such gameID");
        }
        GameData updatedGameData;
        if (joinGameRequest.playerColor().equals("WHITE") && gameData.whiteUsername() == null) {
            updatedGameData = new GameData(gameData.gameID(), username,
                    gameData.blackUsername(), gameData.gameName(), gameData.game());
        }
        else if (joinGameRequest.playerColor().equals("BLACK") && gameData.blackUsername() == null) {
            updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    username, gameData.gameName(), gameData.game());
        }
        else {
            throw new DataAccessException(403, "Error: color taken");
        }
        gameAccess.updateGame(updatedGameData);
    }
}
