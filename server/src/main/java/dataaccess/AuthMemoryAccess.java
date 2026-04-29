package dataaccess;

import data.AuthData;

import java.util.HashMap;

public class AuthMemoryAccess implements AuthDAO{
    final private HashMap<String, AuthData> data = new HashMap<>();

    @Override
    public void createAuth(AuthData a) throws DataAccessException {
        data.put(a.authToken(), a);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return data.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        data.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }
}
