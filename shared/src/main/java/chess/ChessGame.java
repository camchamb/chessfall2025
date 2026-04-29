package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentPlayer = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();
    private ChessPosition enPassantPosition = null;
    private boolean whiteCanCastleRight = true;
    private boolean blackCanCastleRight = true;
    private boolean whiteCanCastleLeft = true;
    private boolean blackCanCastleLeft = true;
    public boolean gameOver = false;

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentPlayer;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentPlayer = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whiteCanCastleRight == chessGame.whiteCanCastleRight && blackCanCastleRight == chessGame.blackCanCastleRight && whiteCanCastleLeft == chessGame.whiteCanCastleLeft && blackCanCastleLeft == chessGame.blackCanCastleLeft && gameOver == chessGame.gameOver && currentPlayer == chessGame.currentPlayer && Objects.equals(board, chessGame.board) && Objects.equals(enPassantPosition, chessGame.enPassantPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentPlayer, board, enPassantPosition, whiteCanCastleRight, blackCanCastleRight, whiteCanCastleLeft, blackCanCastleLeft, gameOver);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (this.board.getPiece(startPosition) == null) {
            return null;
        }
        var piece = this.board.getPiece(startPosition);
        var moves = piece.pieceMoves(this.board, startPosition);
        ArrayList<ChessMove> newMoves = new ArrayList<>();
        for (var move : moves) {
            var newBoard = board.clone();
            movePieceOnBoard(newBoard, move);
            if (!inCheckHelper(newBoard, piece.getTeamColor())) {
                newMoves.add(move);
            }
        }
        castleMove(startPosition, newMoves);
        var enPassant = enPassantMove(startPosition);
        if (enPassant != null) {
            newMoves.add(enPassant);
        }
        return newMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameOver) {
            throw new InvalidMoveException("Game is finished");
        }
        var moves = validMoves(move.getStartPosition());
        if (moves == null || moves.isEmpty()) {
            throw new InvalidMoveException("Not a piece there");
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != currentPlayer) {
            throw new InvalidMoveException("Wrong Team");
        }
        if (!moves.contains(move)) {
            throw new InvalidMoveException("Not a valid move");
        }
        movePieceOnBoard(this.board, move);
        if (currentPlayer.equals(TeamColor.WHITE)) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
        var piece = board.getPiece(move.getEndPosition());
        if (piece.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if (didPawnJump(move)) {
                enPassantPosition = new ChessPosition(move.getEndPosition().getRow(), move.getEndPosition().getColumn());
            } else {
                enPassantPosition = null;
            }
        } else {
            enPassantPosition = null;
        }
        if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            if (Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2) {
                if (move.getEndPosition().equals(new ChessPosition(1, 3))) {
                    var rookPiece = board.getPiece(new ChessPosition(1, 1));
                    board.addPiece(new ChessPosition(1, 1), null);
                    board.addPiece(new ChessPosition(1, 4), rookPiece);
                }
                if (move.getEndPosition().equals(new ChessPosition(1, 7))) {
                    var rookPiece = board.getPiece(new ChessPosition(1, 8));
                    board.addPiece(new ChessPosition(1, 8), null);
                    board.addPiece(new ChessPosition(1, 6), rookPiece);
                }
                if (move.getEndPosition().equals(new ChessPosition(8, 3))) {
                    var rookPiece = board.getPiece(new ChessPosition(8, 1));
                    board.addPiece(new ChessPosition(8, 1), null);
                    board.addPiece(new ChessPosition(8, 4), rookPiece);
                }
                if (move.getEndPosition().equals(new ChessPosition(8, 7))) {
                    var rookPiece = board.getPiece(new ChessPosition(8, 8));
                    board.addPiece(new ChessPosition(8, 8), null);
                    board.addPiece(new ChessPosition(8, 6), rookPiece);
                }
            }
        }

        updateCastle(piece, move.getStartPosition());
    }


    private void movePieceOnBoard(ChessBoard newBoard, ChessMove move) {
        if (move.getPromotionPiece() != null) {
            ChessPiece piece = new ChessPiece(newBoard.getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece());
            newBoard.addPiece(move.getEndPosition(), piece);
            newBoard.addPiece(move.getStartPosition(), null);
        } else {
            ChessPiece piece = newBoard.getPiece(move.getStartPosition());
            if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
                if (piece.getTeamColor().equals(TeamColor.WHITE)) {
                    newBoard.setWhiteKing(move.getEndPosition());
                } else {
                    newBoard.setBlackKing(move.getEndPosition());
                }
            }
            if (piece.getPieceType().equals(ChessPiece.PieceType.PAWN)
                    && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                    && board.getPiece(move.getEndPosition()) == null) {
                var position = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn());
                newBoard.addPiece(position, null);
            }
            newBoard.addPiece(move.getEndPosition(), piece);
            newBoard.addPiece(move.getStartPosition(), null);

        }
    }


    private boolean inCheckHelper(ChessBoard board, TeamColor teamColor) {
        setKingsPosition(board);
        ChessPosition kingPosition;
        if (teamColor.equals(TeamColor.WHITE)) {
            kingPosition = board.getWhiteKing();
        } else {
            kingPosition = board.getBlackKing();
        }
        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                var position = new ChessPosition(y, x);
                if (board.getPiece(position) != null) {
                    var piece = board.getPiece(position);
                    var moves = piece.pieceMoves(board, position);

                    if (piece.getTeamColor() != teamColor
                            && (moves.contains(new ChessMove(position, kingPosition, null))
                            || moves.contains(new ChessMove(position, kingPosition, ChessPiece.PieceType.QUEEN))
                            || moves.contains(new ChessMove(position, kingPosition, ChessPiece.PieceType.ROOK))
                            || moves.contains(new ChessMove(position, kingPosition, ChessPiece.PieceType.BISHOP))
                            || moves.contains(new ChessMove(position, kingPosition, ChessPiece.PieceType.KNIGHT)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return inCheckHelper(this.board, teamColor);
    }




    private boolean noMoves(ChessBoard board, TeamColor teamColor) {
        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                var position = new ChessPosition(y, x);
                var piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    var moves = validMoves(position);
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        if (noMoves(board, teamColor)) {
            gameOver = true;
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        if (noMoves(board, teamColor)) {
            gameOver = true;
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = null;
        setKingsPosition(board);
        this.board = board;
        whiteCanCastleRight = true;
        blackCanCastleRight = true;
        whiteCanCastleLeft = true;
        blackCanCastleLeft = true;
    }


    private void setKingsPosition(ChessBoard board) {
        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                var position = new ChessPosition(y, x);
                var piece = board.getPiece(position);
                if (piece != null && piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
                    if (piece.getTeamColor().equals(TeamColor.WHITE)) {
                        board.setWhiteKing(position);
                    } else {
                        board.setBlackKing(position);
                    }
                }
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private boolean didPawnJump(ChessMove move) {
        return Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) == 2;
    }


    private ChessMove enPassantMove(ChessPosition startPosition) {
        if (enPassantPosition == null) {
            return null;
        }
        var piece = board.getPiece(startPosition);
        var pieceColor = piece.getTeamColor();
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return null;
        }
        ChessPosition endPosition = null;
        if (pieceColor.equals(TeamColor.WHITE)) {
            if (enPassantPosition.equals(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1))) {
                endPosition = new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() + 1);
            }
            if (enPassantPosition.equals(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1))) {
                endPosition = new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() - 1);
            }

        } else {
            if (enPassantPosition.equals(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1))) {
                endPosition = new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() + 1);
            }
            if (enPassantPosition.equals(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1))) {
                endPosition = new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() - 1);
            }
        }
        if (endPosition == null) {
            return null;
        }
        return new ChessMove(startPosition, endPosition, null);
    }


    private void updateCastle(ChessPiece piece, ChessPosition startPosition) {
        if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            if (piece.getTeamColor().equals(TeamColor.WHITE)) {
                whiteCanCastleRight = false;
                whiteCanCastleLeft = false;
            } else {
                blackCanCastleRight = false;
                blackCanCastleLeft = false;
            }
        } else if (piece.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
            if (piece.getTeamColor().equals(TeamColor.WHITE)) {
                if (startPosition.equals(new ChessPosition(1, 1))) {
                    whiteCanCastleLeft = false;
                }
                if (startPosition.equals(new ChessPosition(1, 8))) {
                    whiteCanCastleRight = false;
                } else {
                    if (startPosition.equals(new ChessPosition(8, 1))) {
                        blackCanCastleLeft = false;
                    }
                    if (startPosition.equals(new ChessPosition(8, 8))) {
                        blackCanCastleRight = false;
                    }
                }

            }
        }
    }

    private void castleWhite(ChessPosition startPosition, Collection<ChessMove> moves, ChessPiece piece) {
        if (whiteCanCastleLeft) {
            if (board.getPiece(new ChessPosition(1, 1)) != null
                    && board.getPiece(new ChessPosition(1, 1)).getTeamColor().equals(TeamColor.WHITE)
                    && board.getPiece(new ChessPosition(1, 2)) == null
                    && board.getPiece(new ChessPosition(1, 3)) == null
                    && board.getPiece(new ChessPosition(1, 4)) == null) {
                if (moves.contains(new ChessMove(startPosition, new ChessPosition(1, 4), null))) {
                    var newBoard = board.clone();
                    var move = new ChessMove(startPosition, new ChessPosition(1, 3), null);
                    movePieceOnBoard(newBoard, move);
                    if (!inCheckHelper(newBoard, piece.getTeamColor())) {
                        moves.add(move);
                    }
                }
            }
        }
        if (whiteCanCastleRight) {
            if (board.getPiece(new ChessPosition(1, 8)) != null
                    && board.getPiece(new ChessPosition(1, 8)).getTeamColor().equals(TeamColor.WHITE)
                    && board.getPiece(new ChessPosition(1, 7)) == null
                    && board.getPiece(new ChessPosition(1, 6)) == null) {
                if (moves.contains(new ChessMove(startPosition, new ChessPosition(1, 6), null))) {
                    var newBoard = board.clone();
                    var move = new ChessMove(startPosition, new ChessPosition(1, 7), null);
                    movePieceOnBoard(newBoard, move);
                    if (!inCheckHelper(newBoard, piece.getTeamColor())) {
                        moves.add(move);
                    }
                }
            }
        }
    }

    private void castleBlack(ChessPosition startPosition, Collection<ChessMove> moves, ChessPiece piece) {
        if (blackCanCastleLeft) {
            if (board.getPiece(new ChessPosition(8, 1)) != null
                    && board.getPiece(new ChessPosition(8, 1)).getTeamColor().equals(TeamColor.BLACK)
                    && board.getPiece(new ChessPosition(8, 2)) == null
                    && board.getPiece(new ChessPosition(8, 3)) == null
                    && board.getPiece(new ChessPosition(8, 4)) == null) {
                if (moves.contains(new ChessMove(startPosition, new ChessPosition(8, 4), null))) {
                    var newBoard = board.clone();
                    var move = new ChessMove(startPosition, new ChessPosition(8, 3), null);
                    movePieceOnBoard(newBoard, move);
                    if (!inCheckHelper(newBoard, piece.getTeamColor())) {
                        moves.add(move);
                    }
                }
            }
        }
        if (blackCanCastleRight) {
            if (board.getPiece(new ChessPosition(8, 8)) != null
                    && board.getPiece(new ChessPosition(8, 8)).getTeamColor().equals(TeamColor.BLACK)
                    && board.getPiece(new ChessPosition(8, 7)) == null
                    && board.getPiece(new ChessPosition(8, 6)) == null) {
                if (moves.contains(new ChessMove(startPosition, new ChessPosition(8, 6), null))) {
                    var newBoard = board.clone();
                    var move = new ChessMove(startPosition, new ChessPosition(8, 7), null);
                    movePieceOnBoard(newBoard, move);
                    if (!inCheckHelper(newBoard, piece.getTeamColor())) {
                        moves.add(move);
                    }
                }
            }
        }
    }


    private void castleMove(ChessPosition startPosition, Collection<ChessMove> moves) {
        var piece = board.getPiece(startPosition);
        if (piece == null) {return;}
        var pieceColor = piece.getTeamColor();
        if (piece.getPieceType() != ChessPiece.PieceType.KING) {
            return;
        }
        if (isInCheck(pieceColor)) {return;}
        if (pieceColor.equals(TeamColor.WHITE)) {
            castleWhite(startPosition, moves, piece);
        } else {
            castleBlack(startPosition, moves, piece);
        }
    }


}
