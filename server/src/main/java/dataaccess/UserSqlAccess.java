package dataaccess;

import data.UserData;

import java.sql.*;


public class UserSqlAccess implements UserDAO{
    public UserSqlAccess() throws DataAccessException {
        configureDatabase();
    }


    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM user";
        SqlUtils.executeUpdate(statement);
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES(?, ?, ?)";
        SqlUtils.executeUpdate(statement, u.username(), u.password(), u.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        var password = rs.getString("password");
                        var email = rs.getString("email");

                        return new UserData(username, password, email);
                    }

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void configureDatabase() throws DataAccessException {
            var createUserTable = """
            CREATE TABLE  IF NOT EXISTS user (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
            )""";
            SqlUtils.configureDatabase(createUserTable);
    }
}
