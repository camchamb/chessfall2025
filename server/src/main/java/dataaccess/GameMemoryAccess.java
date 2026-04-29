package dataaccess;

import chess.ChessGame;
import data.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class GameMemoryAccess implements GameDAO{
    private int nextId = 1234;
    final private HashMap<Integer, GameData> data = new HashMap<>();

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextId;
        nextId++;
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        data.put(gameID, gameData);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (!data.containsKey(gameID)) {
            return null;
        }
        return data.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var gameList = new ArrayList<GameData>();
        for (var gameData : data.values()) {
            gameList.add(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), null));
        }
        return gameList;
    }


    @Override
    public void updateGame(GameData u) throws DataAccessException {
        data.put(u.gameID(), u);
    }

    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }
}
