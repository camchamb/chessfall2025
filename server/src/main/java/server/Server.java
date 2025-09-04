package server;

import dataaccess.*;
import io.javalin.*;
import com.google.gson.Gson;
import io.javalin.http.Context;

import io.javalin.http.HttpStatus;
import service.*;
import service.requests.*;


public class Server {

    private final Javalin javalin;

    UserDAO userAccess;
    GameDAO gameAccess;
    AuthDAO authAccess;
    UserService userService;
    GameService gameService;
    Gson serializer = new Gson();

    public Server() {
        try {
            userAccess = new UserMemoryAccess();
            gameAccess = new GameMemoryAccess();
            authAccess = new AuthMemoryAccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        } catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }

        this.userService = new UserService(userAccess, authAccess);
        this.gameService = new GameService(gameAccess, authAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", this::register)
                .delete("/db", this::db)
                .post("/session", this::login)
                .delete("/session", this::logout)
                .get("/game", this::listGames)
                .post("/game", this::createGame)
                .put("/game", this::joinGame);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void register(Context context) {
        try {
            var regReq = serializer.fromJson(context.body(), RegisterRequest.class);
            RegisterResult regRes = userService.register(regReq);
            context.json(serializer.toJson(regRes));
            System.out.println(serializer.toJson(regRes));
//            return response.body();
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
//            return response.body();
        }
    }

    private void db(Context context) {
        try {
            userService.clear();
            gameService.clear();
            context.json("{ }");
        } catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }

    private void login(Context context) {
        try {
            var loginRequest = serializer.fromJson(context.body(), LoginRequest.class);
            LoginResult loginResult = userService.login(loginRequest);
            context.json(serializer.toJson(loginResult));
            System.out.println(serializer.toJson(loginResult));
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }

    private void logout(Context context) {
        try {
            String authToken = context.header("authorization");
            userService.logout(new LogoutRequest(authToken));
            context.json("{ }");
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }

    private void listGames(Context context) {
        try {
            String authToken = context.header("authorization");
            var listGamesRequest = new ListGamesRequest(authToken);
            ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);
            context.json(serializer.toJson(listGamesResult));
            System.out.println(serializer.toJson(listGamesResult));
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }

    private void createGame(Context context) {
        try {
            var tempRequest = serializer.fromJson(context.body(), CreateGameRequest.class);
            var createGameRequest = new CreateGameRequest(tempRequest.gameName(), context.header("authorization"));
            CreateGameResult createGameResult = gameService.createGame(createGameRequest);
            context.json(serializer.toJson(createGameResult));
            System.out.println(serializer.toJson(createGameResult));
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }

    private void joinGame(Context context) {
        try {
            var tempRequest = serializer.fromJson(context.body(), JoinGameRequest.class);
            var joinGameRequest = new JoinGameRequest(tempRequest.playerColor(),
                    tempRequest.gameID(), context.header("authorization"));
            gameService.joinGame(joinGameRequest);
            context.json("{ }");
        }
        catch (DataAccessException ex) {
            errorHandling(ex, context);
        }
    }


    public void errorHandling(DataAccessException ex, Context context) {
        context.status(ex.getStatus());
        String message = ex.getMessage();
        String json = "{\"message\": \"" + message + "\" }";
        System.out.println(json);
        context.json(json);
    }
}
