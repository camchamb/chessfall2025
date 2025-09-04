package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData a) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}
