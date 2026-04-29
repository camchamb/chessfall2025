package client;

import chess.*;
//import com.sun.nio.sctp.NotificationHandler;
//import client.websocket.NotificationHandler;
//import client.websocket.WebSocketFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import data.GameData;
import serverFacade.ServerFacade;
import requests.*;
import data.UserData;
import ui.PrintBoard;
import websocket.messages.ServerMessage;
//import ui.PrintBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

public class ChessClient {
    private String authToken = null;
    private ArrayList<GameData> gameList = null;
    private final ServerFacade server;
    private final String serverUrl;
    public State state = State.PreloginClient;
    private ChessGame.TeamColor playersColor = null;
    public ChessGame game;
    private int currentGameId = -1;
    private WebSocketFacade ws;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (state) {
                case PreloginClient -> preEval(params, cmd);
                case PostloginClient -> postEval(params, cmd);
                case GamePlayClient -> gameEval(params, cmd);
            };
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    public String preEval(String[] params, String cmd) throws RuntimeException{
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            default -> preHelp();
        };
    }

    public String postEval(String[] params, String cmd) throws RuntimeException{
        return switch (cmd) {
            case "create" -> create(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
            case "logout" -> logout();
            case "quit" -> quit();
            default -> postHelp();
        };
    }

    public String gameEval(String[] params, String cmd) throws RuntimeException{
        return switch (cmd) {
            case "move" -> move(params);
            case "resign" -> resign();
            case "redraw" -> redraw(new ArrayList<ChessMove>());
            case "quit" -> quit();
            case "highlight" -> highlight(params);
            case "leave" -> leave();
            default -> gameHelp();
        };
    }

    public String preHelp() {
        return """
                register <username> <password> <email> - to register
                login <username> <password> - to login
                quit - stop client
                help - All commands""";}

    public String postHelp() {
        return """
                create <GameName> - to create a game
                list - list all games
                join <ID> <WHITE|BLACK> - Join game as a color
                observe <ID> - observe game
                logout - logout of server
                quit - stop client
                help - All commands""";}

    public String gameHelp() {
        return """
                redraw - Redraws board
                leave - Leaves game
                move <Position> <Position> <Promotion Piece>- Moves a piece from first position to second
                resign - If your losing too bad
                highlight - Highlight Legal Moves
                quit - Stop client
                help - All commands""";}

    public String register(String... params) throws RuntimeException {
        if (params.length < 3) {
            throw new RuntimeException("Expected: <username> <password> <email>");
        }
        var username = params[0];
        var password = params[1];
        var email = params[2];
        var user = server.register(new RegisterRequest(username, password, email));
        if (user.authToken() == null) {throw new RuntimeException("Null authToken");}
        state = State.PostloginClient;
        authToken = user.authToken();
        return postHelp();
    }

    public String login(String... params) throws RuntimeException {
        if (params.length < 2) {
            throw new RuntimeException("Expected: <username> <password>");
        }
        var username = params[0];
        var password = params[1];
        var user = server.login(new LoginRequest(username, password));
        state = State.PostloginClient;
        authToken = user.authToken();
        return postHelp();
    }

    public String create(String... params) throws RuntimeException {
        if (params.length < 1) {
            throw new RuntimeException("Expected: <username> <password>");
        }
        var gameName = params[0];
        server.create(new CreateGameRequest(gameName, authToken));
        return "Created game: " + gameName;
    }

    public String list() throws RuntimeException {
        var result = server.list(authToken);
        gameList = (ArrayList<GameData>) result;
        StringBuilder games = new StringBuilder();
        int i = 1;
        for (var game : result) {
            games.append("Game ");
            games.append(i).append(":\n");
            games.append(game.toString());
            games.append("\n");
            i++;
        }
        return games.toString();
    }

    public String logout() throws RuntimeException {
        server.logout(new LogoutRequest(authToken));
        state = State.PreloginClient;
        authToken = null;
        return "Logged out";
    }

    public String quit() throws RuntimeException {
        if (state.equals(State.GamePlayClient)) {
//            ws.leave(authToken, currentGameId);
        }
        server.logout(new LogoutRequest(authToken));
        state = State.PreloginClient;
        return "quit";
    }

    public String join(String... params) throws RuntimeException {
        if (params.length < 2) {
            throw new RuntimeException("Expected: <ID> <COLOR>");
        }
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new RuntimeException("GameID not a number");
        }
        if (gameID < 1 || gameID > gameList.size()) {
            throw new RuntimeException("GameID not valid");
        }
        var color = params[1].toUpperCase();
        int realGameId = gameList.get(gameID - 1).gameID();
        server.join(new JoinGameRequest(color, realGameId, authToken));
        try {
            ws = new WebSocketFacade(serverUrl, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect Websocket");
        }
        ws.connect(authToken, realGameId);
        currentGameId = realGameId;
        state = State.GamePlayClient;
        if (color.equals("WHITE")) {
            playersColor = ChessGame.TeamColor.WHITE;
        } else {
            playersColor = ChessGame.TeamColor.BLACK;
        }
        return "";
//        PrintBoard.printBoard(playersColor, );
    }

    public String observe(String... params) throws RuntimeException {
        if (params.length < 1) {
            throw new RuntimeException("Expected: <ID>");
        }
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new RuntimeException("GameID not a number");
        }
        if (gameID < 1 || gameID > gameList.size()) {
            throw new RuntimeException("GameID not valid");
        }
        GameData obsGame = gameList.get(gameID - 1);
        try {
            ws = new WebSocketFacade(serverUrl, this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect Websocket");
        }
        ws.connect(authToken, obsGame.gameID());
        currentGameId = obsGame.gameID();
        state = State.GamePlayClient;
        playersColor = ChessGame.TeamColor.WHITE;
        return "";
    }

    public String printPrompt() {
        return switch (state) {
            case PreloginClient -> "Logged Out";
            case PostloginClient -> "Logged In";
            case GamePlayClient -> "GamePlay";
        };
    }

    public String redraw(Collection<ChessMove> chessMoves) {
        if (playersColor.equals(ChessGame.TeamColor.WHITE)) {
            PrintBoard.printBoard(ChessGame.TeamColor.WHITE, game, chessMoves);
        } else {
            PrintBoard.printBoard(ChessGame.TeamColor.BLACK, game, chessMoves);
        }
        return "";
    }

    public String leave() {
        ws.leave(authToken, currentGameId);
        state = State.PostloginClient;
        currentGameId = -1;
        return "";
    }

    public String resign() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure?");
        String line = scanner.nextLine();
        if (line.toLowerCase().contains("yes") || line.toLowerCase().contains("y")) {
            ws.resign(authToken, currentGameId);
            return "";
        }
        return "";
    }

    public String highlight(String... params) {
        if (params.length < 1) {
            throw new RuntimeException("Expected: <Position>");
        }
        if (params[0].length() != 2) {
            throw new RuntimeException("Expected: <LetterNumber> for position");
        }
        if (!Character.isDigit(params[0].charAt(1)) || Character.isDigit(params[0].charAt(0))) {
            throw new RuntimeException("Expected: <LetterNumber> for position");
        }
        var startPostition = new ChessPosition(Integer.parseInt(params[0].replaceAll("[\\D]", "")), letterToNum(params[0].charAt(0)));
        redraw(game.validMoves(startPostition));
        return "";
    }

    public String move(String... params) {
        ChessPiece.PieceType promotionPiece;
        if (game.gameOver) {
            throw new RuntimeException("Error: Game is Finished");
        }
        if (!game.getTeamTurn().equals(playersColor)) {
            throw new RuntimeException("Error: Not your turn");
        }
        if (params.length < 2) {
            throw new RuntimeException("Expected: <Position> <Position>");
        }
        if (params.length == 2) {
            promotionPiece = null;
        } else {
            promotionPiece = toChessPiece(params[2]);
        }
        if (params[0].length() != 2 || params[1].length() != 2) {
            throw new RuntimeException("Expected: <LetterNumber> for position");
        }
        if (!Character.isDigit(params[0].charAt(1)) || !Character.isDigit(params[1].charAt(1))) {
            throw new RuntimeException("Expected: <LetterNumber> for position");
        }
        if (Character.isDigit(params[0].charAt(0)) || Character.isDigit(params[1].charAt(0))) {
            throw new RuntimeException("Expected: <LetterNumber> for position");
        }
        var startPostition = new ChessPosition(Integer.parseInt(params[0].replaceAll("[\\D]", "")), letterToNum(params[0].charAt(0)));
        var endPostition = new ChessPosition(Integer.parseInt(params[1].replaceAll("[\\D]", "")), letterToNum(params[1].charAt(0)));
        ChessMove move = new ChessMove(startPostition, endPostition, promotionPiece);
        ws.makeMove(authToken, currentGameId, move);
        return "";
    }

    public void notify(ServerMessage notification) {
        if (notification.getServerMessageType().equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
            PrintBoard.printBoard(playersColor, notification.getGame(), new ArrayList<>());
            game = notification.getGame();
        }
        if (notification.getServerMessageType().equals(ServerMessage.ServerMessageType.NOTIFICATION)) {
            System.out.println(notification.getMessage());
        }
        if (notification.getServerMessageType().equals(ServerMessage.ServerMessageType.ERROR)) {
            System.out.println(notification.getMessage());
        }
        System.out.print(printPrompt() + " >>> ");
    }


    private ChessPiece.PieceType toChessPiece(String piece) {
        return switch (piece) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new RuntimeException("Not a valid promotion piece");
        };
    }

    private int letterToNum(Character c) {
        return switch (c) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new RuntimeException("Expected: <LetterNumber> for position");
        };
    }

}
