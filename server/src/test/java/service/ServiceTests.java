package service;

import data.GameData;
import dataaccess.*;
import org.junit.jupiter.api.*;
import requests.*;

public class ServiceTests {
    private static final UserDAO USER_ACCESS = new UserMemoryAccess();
    private static final GameDAO GAME_ACCESS = new GameMemoryAccess();
    private static final AuthDAO AUTH_ACCESS = new AuthMemoryAccess();

    private static final UserService USER_SERVICE = new UserService(USER_ACCESS, AUTH_ACCESS);
    private static final GameService GAME_SERVICE = new GameService(GAME_ACCESS, AUTH_ACCESS);

    @Test
    @Order(1)
    @DisplayName("user register")
    public void userRegister() throws DataAccessException {
        USER_SERVICE.clear();
        GAME_SERVICE.clear();
        var registerRequest = new RegisterRequest("username", "password", "email.com");
        var registerResult = new RegisterResult("username", "123456");
        Assertions.assertEquals(registerResult.username(), USER_SERVICE.register(registerRequest).username(),
                "Not right username");
    }

    @Test
    @Order(2)
    @DisplayName("invalid user register")
    public void invalidUserRegister() {
        var invalidRequest = new RegisterRequest("ya", "no", null);
        Assertions.assertThrows(DataAccessException.class, () -> USER_SERVICE.register(invalidRequest));
    }

    @Test
    @Order(3)
    @DisplayName("user login")
    public void userLogin() throws DataAccessException {
        USER_SERVICE.clear();
        userRegister();
        var loginResult = new LoginResult("username", "123456");
        Assertions.assertEquals(loginResult.username(),
                USER_SERVICE.login(new LoginRequest("username", "password")).username(),
                "Not right username");
    }

    @Test
    @Order(4)
    @DisplayName("invalid user login")
    public void invalidUserLogin() throws DataAccessException {
        USER_SERVICE.clear();
        userRegister();
        var invalidRequest = new RegisterRequest("ya", "no", null);
        Assertions.assertThrows(DataAccessException.class, () -> USER_SERVICE.register(invalidRequest));
    }

    @Test
    @Order(5)
    @DisplayName("user logout")
    public void userLogout() throws DataAccessException {
        USER_SERVICE.clear();
        userRegister();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        var logoutRequest = new LogoutRequest(loginResult.authToken());
        USER_SERVICE.logout(logoutRequest);
    }

    @Test
    @Order(6)
    @DisplayName("invalid user logout")
    public void invalidUserLogout() throws DataAccessException {
        USER_SERVICE.clear();
        userRegister();
        USER_SERVICE.login(new LoginRequest("username", "password"));
        var invalidLogoutRequest = new LogoutRequest("1234");
        Assertions.assertThrows(DataAccessException.class, () ->  USER_SERVICE.logout(invalidLogoutRequest));
    }

    @Test
    @Order(7)
    @DisplayName("Create game")
    public void gameCreate() throws DataAccessException {
        USER_SERVICE.clear();
        GAME_SERVICE.clear();
        userRegister();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        String authToken = loginResult.authToken();
        var createGameRequest = new CreateGameRequest("gamename", authToken);
        GAME_SERVICE.createGame(createGameRequest);
    }

    @Test
    @Order(8)
    @DisplayName("invalid game create")
    public void invalidGameCreate() throws DataAccessException {
        clear();
    }

    @Test
    @Order(9)
    @DisplayName("List games")
    public void gameList() throws DataAccessException {
        USER_SERVICE.clear();
        GAME_SERVICE.clear();
        gameCreate();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        String authToken = loginResult.authToken();
        var createGameRequest = new CreateGameRequest("gamename", authToken);
        GAME_SERVICE.createGame(createGameRequest);
    }

    @Test
    @Order(10)
    @DisplayName("invalid Listgames")
    public void invalidGameList() throws DataAccessException {
        clear();
    }

    private void clear() throws DataAccessException {
        USER_SERVICE.clear();
        GAME_SERVICE.clear();
        userRegister();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        loginResult.authToken();
        var createGameRequest = new CreateGameRequest("gamename", "wrong");
        Assertions.assertThrows(DataAccessException.class, () -> GAME_SERVICE.createGame(createGameRequest));
    }

    @Test
    @Order(11)
    @DisplayName("join game")
    public void joinGame() throws DataAccessException {
        USER_SERVICE.clear();
        GAME_SERVICE.clear();
        userRegister();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        String authToken = loginResult.authToken();
        var createGameRequest = new CreateGameRequest("gamename", authToken);
        var createGameResult = GAME_SERVICE.createGame(createGameRequest);
        var joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameID(), authToken);
        GAME_SERVICE.joinGame(joinGameRequest);
        var listGamesResult = GAME_SERVICE.listGames(new ListGamesRequest(authToken));
        var testObject = new GameData(createGameResult.gameID(), "username", null, "gamename", null);
        assert listGamesResult.games().contains(testObject);
    }

    @Test
    @Order(12)
    @DisplayName("invalid join game")
    public void invalidJoinGame() throws DataAccessException {
        GAME_SERVICE.clear();
        userRegister();
        var loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        String authToken = loginResult.authToken();
        var createGameRequest = new CreateGameRequest("gamename", authToken);
        var createGameResult = GAME_SERVICE.createGame(createGameRequest);
        var badJoinGameRequest = new JoinGameRequest("GREEN", createGameResult.gameID(), authToken);
        Assertions.assertThrows(DataAccessException.class, () -> GAME_SERVICE.joinGame(badJoinGameRequest));
    }

    @Test
    @Order(12)
    @DisplayName("Check clear")
    public void clearAll() throws DataAccessException {
        userRegister();
        USER_SERVICE.clear();
        var loginRequest = new LoginRequest("username", "password");
        Assertions.assertThrows(DataAccessException.class, () -> USER_SERVICE.login(loginRequest));
    }

}