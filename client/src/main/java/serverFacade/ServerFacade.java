package serverFacade;

import chess.ResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.*;
import requests.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

//import java.io.*;
//import java.net.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Locale;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private static final HttpClient client = HttpClient.newHttpClient();
    private Gson gson = new Gson();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest req) {
        var response = sendRequest("/user", "POST", new Gson().toJson(req), "");
        checkSuccess(response);
        return new Gson().fromJson(response.body(), RegisterResult.class);
    }

    public LoginResult login(LoginRequest req) {
        var response = sendRequest("/session", "POST", new Gson().toJson(req), "");
        checkSuccess(response);
        return new Gson().fromJson(response.body(), LoginResult.class);
    }

    public CreateGameResult create(CreateGameRequest req) {
        var response = sendRequest("/game", "POST", new Gson().toJson(req), req.authToken());
        checkSuccess(response);
        return new Gson().fromJson(response.body(), CreateGameResult.class);
    }

    public Collection<GameData> list(String authToken) {
        var response = sendRequest("/game", "GET", "", authToken);
        checkSuccess(response);
        return new Gson().fromJson(response.body(), ListGamesResult.class).games();
    }

    public void join(JoinGameRequest req) {
        var response = sendRequest("/game", "PUT", new Gson().toJson(req), req.authToken());
        checkSuccess(response);
    }

    public void logout(LogoutRequest req) {
        var response = sendRequest("/session", "DELETE", new Gson().toJson(req), req.authToken());
        checkSuccess(response);
    }

    public void clear() {
        var response = sendRequest("/db", "DELETE", "", "");
        checkSuccess(response);
    }

    private HttpResponse<String> sendRequest(String path, String method, String jsonBody, String header) {
        try {
        var request = HttpRequest.newBuilder(URI.create(serverUrl + path))
                .method(method, requestBodyPublisher(jsonBody))
                .header("Authorization", header) //TODO NULL EXCEPTION
                .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest.BodyPublisher requestBodyPublisher(String body) throws IOException {
        if (body != null) {
            return HttpRequest.BodyPublishers.ofString(body);
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private void checkSuccess(HttpResponse<String> response) {
        var statusCode = response.statusCode();
        if (statusCode != 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            throw new RuntimeException(json.get("message").getAsString());
        }
    }



//    public <T> T makeRequest(String method, String path, Object request, Class<T> objectClass, String authToken) {
//        try {
//            URL url = (new URI(serverUrl + path)).toURL();
//            HttpURLConnection http = (HttpURLConnection) url.openConnection();
//            http.setRequestMethod(method);
//            http.setDoOutput(true);
//
//            writeBody(request, http, authToken);
//            http.connect();
//            throwIfNotSuccessful(http);
//            return readBody(http, objectClass);
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }

//    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
//        T response = null;
//        if (http.getContentLength() < 0) {
//            try (InputStream respBody = http.getInputStream()) {
//                InputStreamReader reader = new InputStreamReader(respBody);
//                if (responseClass != null) {
//                    response = new Gson().fromJson(reader, responseClass);
//                }
//            }
//        }
//        return response;
//    }
//
//    private static void writeBody(Object request, HttpURLConnection http, String authToken) throws IOException {
//        http.setRequestProperty ("authorization", authToken);
//        if (request != null) {
//            http.addRequestProperty("Content-Type", "application/json");
//            String reqData = new Gson().toJson(request);
//            try (OutputStream reqBody = http.getOutputStream()) {
//                reqBody.write(reqData.getBytes());
//            }
//        }
//    }
//
//    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, RuntimeException {
//        var status = http.getResponseCode();
//        if (!isSuccessful(status)) {
//            try (InputStream respErr = http.getErrorStream()) {
//                if (respErr != null) {
//                    try {
//                        throw ResponseException.fromJson(respErr);
//                    } catch (ResponseException e) {
//                        throw new RuntimeException(e.getMessage());
//                    }
//                }
//            }
//
//            throw new RuntimeException("other failure: " + status);
//        }
//    }
//
//    private boolean isSuccessful(int status) {
//        return status / 100 == 2;
//    }
}
