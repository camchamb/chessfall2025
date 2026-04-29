package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class PrintBoard {

    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    private static ChessBoard board;

    private static Set<ChessPosition> endPositions;
    private static ChessPosition startPosition;

    public static void printBoard(ChessGame.TeamColor color, ChessGame game, Collection<ChessMove> chessMoves) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        startPosition = null;
        endPositions = new HashSet<>();

        if (!chessMoves.isEmpty()) {
            startPosition = chessMoves.iterator().next().getStartPosition();
        }
        for (var move : chessMoves) {
            endPositions.add(move.getEndPosition());
        }

        board = game.getBoard();

        out.print(ERASE_SCREEN);

        out.print("\n");

        if (color == ChessGame.TeamColor.WHITE) {
            drawWhiteBoard(out);
        } else {
            drawBlackBoard(out);
        }
        out.println();

        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
    }

    private static void drawBlackHeader(PrintStream out) {

        setBlack(out);

//            String[] headers = { " h ", "  g ", "  f ", " e ", "  d ", " c ", "  b " , " a "};
        String[] headers = {EMPTY, " h ", " g ", " f ", " e ", " d ", " c ", " b " , " a "};
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES + 1; ++boardCol) {
            drawHeader(out, headers[boardCol]);
        }

        out.println();
    }

    private static void drawWhiteHeader(PrintStream out) {
        setBlack(out);
//        String[] headers = { " a ", "  b ", "  c ", " d ", "  e ", " f ", "  g " , " h "};
        String[] headers = {EMPTY, " a ", " b ", " c ", " d ", " e ", " f ", " g " , " h "};
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES + 1; ++boardCol) {
            drawHeader(out, headers[boardCol]);
        }

        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        printHeaderText(out, headerText);
    }

    private static void printHeaderText(PrintStream out, String player) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);

        out.print(player);

        setBlack(out);
    }

    private static void drawWhiteBoard(PrintStream out) {
        drawWhiteHeader(out);

        for (int boardRow = 8; boardRow > 0; --boardRow) {
            drawRowLabel(out, boardRow);
            for (int boardCol = 1; boardCol <= 8; ++boardCol) {
                drawBoard(out, boardRow, boardCol);
            }
            drawRowLabel(out, boardRow);
            setBlack(out);
            out.println();
        }

        drawWhiteHeader(out);
    }

    private static void drawBlackBoard(PrintStream out) {
        drawBlackHeader(out);

        for (int boardRow = 1; boardRow <= BOARD_SIZE_IN_SQUARES; ++boardRow) {

            drawRowLabel(out, boardRow);
            for (int boardCol = 8; boardCol > 0; --boardCol) {

                drawBoard(out, boardRow, boardCol);
            }
            drawRowLabel(out, boardRow);
            setBlack(out);
            out.println();
        }

        drawBlackHeader(out);
    }

    private static void drawBoard(PrintStream out, int boardRow, int boardCol) {
        var position = new ChessPosition(boardRow, boardCol);
        if ((boardRow + boardCol) % 2 != 0) {
            if (endPositions.contains(position)) {
                printLightGreenSquare(out, board.getPiece(position));
            } else if (startPosition != null && startPosition.equals(position)) {
                printYellowSquare(out, board.getPiece(position));
            } else {
                printWhiteSquare(out, board.getPiece(position));
            }
        } else {
            if (endPositions.contains(position)) {
                printDarkGreenSquare(out, board.getPiece(position));
            } else if (startPosition != null && startPosition.equals(position)) {
                printOrangeSquare(out, board.getPiece(position));
            } else {
                printBlackSquare(out, board.getPiece(position));
            }
        }
    }

    private static void drawRowLabel(PrintStream out, int boardRow) {
        String[] rows = { " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 " , " 8 "};

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);

        out.print(rows[boardRow - 1]);
    }


    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_LIGHT_GREY);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void printWhiteSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_BLACK);

        out.print(toCharacter(player));

        setWhite(out);
    }

    private static void printBlackSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(toCharacter(player));

        setWhite(out);
    }

    private static void printLightGreenSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_GREEN);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(toCharacter(player));

        setWhite(out);
    }

    private static void printDarkGreenSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(toCharacter(player));

        setWhite(out);
    }

    private static void printYellowSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_YELLOW);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(toCharacter(player));

        setWhite(out);
    }

    private static void printOrangeSquare(PrintStream out, ChessPiece player) {
        out.print(SET_BG_COLOR_MAGENTA);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(toCharacter(player));

        setWhite(out);
    }

    private static String toCharacter(ChessPiece player) {
        if (player == null) {
            return EMPTY;
        }
        var color = player.getTeamColor();
        var type = player.getPieceType();

        return switch (color) {
            case WHITE -> whiteCharacter(type);
            case BLACK -> blackCharacter(type);
        };
    }

    private static String whiteCharacter(ChessPiece.PieceType type) {
        return getString(type, WHITE_KING, WHITE_QUEEN, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WHITE_PAWN);
    }

    private static String getString(ChessPiece.PieceType type, String whiteKing, String whiteQueen,
                                    String whiteBishop, String whiteKnight, String whiteRook, String whitePawn) {
        return switch (type) {
            case KING -> whiteKing;
            case QUEEN -> whiteQueen;
            case BISHOP -> whiteBishop;
            case KNIGHT -> whiteKnight;
            case ROOK -> whiteRook;
            case PAWN -> whitePawn;
        };
    }

    private static String blackCharacter(ChessPiece.PieceType type) {
        return getString(type, BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_PAWN);
    }

}