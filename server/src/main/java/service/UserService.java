package service;

import dataaccess.*;
import data.AuthData;
import data.UserData;
//import org.mindrot.*
import org.mindrot.jbcrypt.BCrypt;
import requests.*;

import java.util.UUID;

public class UserService {

    private final UserDAO userAccess;
    private final AuthDAO authAccess;

    public UserService(UserDAO userAccess, AuthDAO authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new DataAccessException(400, "Error: bad request");
        }
        UserData userData = userAccess.getUser(registerRequest.username());
        if (userData != null) {
            throw new DataAccessException(403, "Error: already taken");
        }
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        userData = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        userAccess.createUser(userData);
        String authToken = generateToken();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        authAccess.createAuth(authData);
        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        if (loginRequest.password() == null || loginRequest.username() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        UserData userData = userAccess.getUser(loginRequest.username());
        if (userData == null) {
            throw new DataAccessException(401, "Error: invalid username");
        }
        if (!BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        String authToken = generateToken();
        var authdata = new AuthData(authToken, loginRequest.username());
        authAccess.createAuth(authdata);
        return new LoginResult(loginRequest.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException{
        if (logoutRequest.authToken() == null) {
            throw new DataAccessException(400, "Error: invalid request");
        }
        var authData = authAccess.getAuth(logoutRequest.authToken());
        if (authData == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        authAccess.deleteAuth(logoutRequest.authToken());
    }

    public void clear() throws DataAccessException{
        userAccess.clear();
        authAccess.clear();
    }
}
