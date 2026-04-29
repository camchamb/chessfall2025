package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import data.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameSqlAccess implements GameDAO {
    private final Gson serializer = new Gson();

    public GameSqlAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO game (gameName, game) VALUES(?, ?)";
        var game = serializer.toJson(new ChessGame(), ChessGame.class);
        return SqlUtils.executeUpdate(statement, gameName, game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        var whiteUsername = rs.getString("whiteUsername");
                        var blackUsername = rs.getString("blackUsername");
                        var gameName = rs.getString("gameName");
                        var game = rs.getString("game");
                        var gameObject = serializer.fromJson(game, ChessGame.class);
                        return new GameData(gameID, whiteUsername, blackUsername, gameName, gameObject);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var allGames = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM game";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var gameID = rs.getInt("gameID");
                        var whiteUsername = rs.getString("whiteUsername");
                        var blackUsername = rs.getString("blackUsername");
                        var gameName = rs.getString("gameName");
                        allGames.add(new GameData(gameID, whiteUsername, blackUsername, gameName, null));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return allGames;
    }

    @Override
    public void updateGame(GameData u) throws DataAccessException {
        var statement = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        var game = serializer.toJson(u.game(), ChessGame.class);
        SqlUtils.executeUpdate(statement, u.whiteUsername(), u.blackUsername(), u.gameName(), game, u.gameID());
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM game";
        SqlUtils.executeUpdate(statement);
    }

    private void configureDatabase() throws DataAccessException {
        var createUserTable = """
            CREATE TABLE  IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255) DEFAULT NULL,
                blackUsername VARCHAR(255) DEFAULT NULL,
                gameName VARCHAR(255) DEFAULT NULL,
                game TEXT DEFAULT NULL,
                PRIMARY KEY (gameID),
                FOREIGN KEY (whiteUsername) REFERENCES user(username) ON DELETE CASCADE,
                FOREIGN KEY (blackUsername) REFERENCES user(username) ON DELETE CASCADE
            )""";

            SqlUtils.configureDatabase(createUserTable);
}
}
