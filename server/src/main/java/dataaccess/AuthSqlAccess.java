package dataaccess;

import data.AuthData;

public class AuthSqlAccess implements AuthDAO {
    public AuthSqlAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void createAuth(AuthData a) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES(?, ?)";
        SqlUtils.executeUpdate(statement, a.authToken(), a.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        var username = rs.getString("username");
                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        SqlUtils.executeUpdate(statement, authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM auth";
        SqlUtils.executeUpdate(statement);
    }

    private void configureDatabase() throws DataAccessException {
        var createUserTable = """
            CREATE TABLE  IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken),
                FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
            )""";

        SqlUtils.configureDatabase(createUserTable);
}
}
