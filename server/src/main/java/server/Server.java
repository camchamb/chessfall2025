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

        UserService userService = new UserService(userAccess, authAccess);
        GameService gameService = new GameService(gameAccess, authAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", this::register);

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


    public void errorHandling(DataAccessException ex, Context context) {
        context.status(HttpStatus.valueOf(ex.getMessage()));
        String message = "Invalid request";
        String json = "{\"message\": \"" + message + "\" }";
        System.out.println(json);
        context.json(json);
    }
}
