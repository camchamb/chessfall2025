package chess;

import java.util.Arrays;
import java.util.Objects;
import static chess.ChessPiece.PieceType.*;
import static chess.ChessGame.TeamColor.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    private ChessPiece[][] board = new ChessPiece[8][8];

    private ChessPosition whiteKing = null;
    private ChessPosition blackKing = null;

    public ChessBoard() {
    }

    public ChessPosition getWhiteKing() {
        return whiteKing;
    }

    public void setWhiteKing(ChessPosition whiteKing) {
        this.whiteKing = whiteKing;
    }

    public ChessPosition getBlackKing() {
        return blackKing;
    }

    public void setBlackKing(ChessPosition blackKing) {
        this.blackKing = blackKing;
    }

    public void setBoard(ChessPiece[][] board) {
        this.board = board.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard clone = (ChessBoard) super.clone();
            ChessPiece[][] gameState = new ChessPiece[8][8];
            for (int i = 0; i < 8; i++) {
                gameState[i] = Arrays.copyOf(clone.getBoard()[i], 8);
            }
            clone.board = gameState;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }


    public ChessPiece[][] getBoard() {
        return board;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] backRow = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};

        board = new ChessPiece[8][8];
        whiteKing = new ChessPosition(1, 5);
        blackKing = new ChessPosition(8, 5);

        for (int col = 0; col < backRow.length; col++) {
            addPiece(new ChessPosition(1, col + 1), new ChessPiece(WHITE, backRow[col]));
            addPiece(new ChessPosition(2, col + 1), new ChessPiece(WHITE, PAWN));
            addPiece(new ChessPosition(8, col + 1), new ChessPiece(BLACK, backRow[col]));
            addPiece(new ChessPosition(7, col + 1), new ChessPiece(BLACK, PAWN));
        }
    }


    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + Arrays.deepToString(board) +
                '}';
    }
}
