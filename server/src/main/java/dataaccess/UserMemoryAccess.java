package dataaccess;

import data.UserData;
import java.util.HashMap;

public class UserMemoryAccess implements UserDAO{
    final private HashMap<String, UserData> data = new HashMap<>();


    @Override
    public void clear() throws DataAccessException {
        data.clear();
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        data.put(u.username(), u);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!data.containsKey(username)) {
            return null;
        }
        return data.get(username);
    }
}
