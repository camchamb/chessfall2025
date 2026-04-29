package dataaccess;

import data.UserData;

public interface UserDAO {
    void clear() throws DataAccessException;

    void createUser(UserData u) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

}
