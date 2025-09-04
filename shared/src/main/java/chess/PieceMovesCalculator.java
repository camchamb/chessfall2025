package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PieceMovesCalculator {

    private final ChessPiece.PieceType type;
    private final ChessGame.TeamColor pieceColor;
    private final ChessBoard board;
    private final ChessPosition myPosition;

    public PieceMovesCalculator(ChessPiece.PieceType type, ChessGame.TeamColor pieceColor, ChessBoard board, ChessPosition myPosition) {
        this.type = type;
        this.pieceColor = pieceColor;
        this.board = board;
        this.myPosition = myPosition;
    }

    public Collection<ChessMove> pieceMoves() {
        ArrayList<ChessMove> moves = new ArrayList<>();
        if (this.type.equals(ChessPiece.PieceType.ROOK)) {
            moves = (ArrayList<ChessMove>) rookMove(moves);
        }
        if (this.type.equals(ChessPiece.PieceType.PAWN)) {
            moves = (ArrayList<ChessMove>) pawnMove(moves);
        }
        if (this.type.equals(ChessPiece.PieceType.KING)) {
            moves = (ArrayList<ChessMove>) kingMove(moves);
        }
        if (this.type.equals(ChessPiece.PieceType.BISHOP)) {
            moves = (ArrayList<ChessMove>) bishopMove(moves);
        }
        if (this.type.equals(ChessPiece.PieceType.QUEEN)) {
            moves = (ArrayList<ChessMove>) rookMove(moves);
            moves = (ArrayList<ChessMove>) bishopMove(moves);
        }
        if (this.type.equals(ChessPiece.PieceType.KNIGHT)) {
            moves = (ArrayList<ChessMove>) knightMove(moves);
        }
        return moves;
    }

    private boolean addSpace(Collection<ChessMove> moves, int y, int x) {
        if (x > 8 || x < 1 || y < 1 || y >8) {return false;}
        var position = new ChessPosition(y, x);
        if (board.getPiece(position) == null) {
            moves.add(new ChessMove(this.myPosition, position, null));
            return true;
        } else {
            if (board.getPiece(position).getTeamColor().equals(pieceColor)) {
                return false;
            } else {
                moves.add(new ChessMove(this.myPosition, position, null));
                return false;
            }
        }
    }

    private boolean addPawnSpace(Collection<ChessMove> moves, int y, int x, boolean forward) {
        if (x > 8 || x < 1 || y < 1 || y > 8) {return false;}
        var position = new ChessPosition(y, x);
        if (forward) {
            if (board.getPiece(position) == null) {
                promotePawn(moves, position);
                return true;
            } else {
                return false;
            }
        } else {
            if (board.getPiece(position) == null) {
                return false;
            } else if (board.getPiece(position).getTeamColor().equals(pieceColor)) {return false;}
            else {
                promotePawn(moves, position);
                return true;
            }
        }
    }

    private void promotePawn(Collection<ChessMove> moves, ChessPosition position) {
        if ((pieceColor.equals(ChessGame.TeamColor.WHITE)
                && myPosition.getRow() == 7) || (pieceColor.equals(ChessGame.TeamColor.BLACK)
                && myPosition.getRow() == 2)) {
            for (var piece : ChessPiece.PieceType.values()) {
                if (piece != ChessPiece.PieceType.KING && piece != ChessPiece.PieceType.PAWN) {
                    moves.add(new ChessMove(this.myPosition, position, piece));
                }
            }
        } else {
            moves.add(new ChessMove(this.myPosition, position, null));
        }
    }

    private Collection<ChessMove> rookMove(Collection<ChessMove> moves) {
        for (int y = myPosition.getRow()-1; y > 0; y--) {
            if (!addSpace(moves, y, this.myPosition.getColumn())) {
                break;
            }
        }
        for (int y = myPosition.getRow()+1; y <= 8; y++) {
            if (!addSpace(moves, y, this.myPosition.getColumn())) {
                break;
            }
        }
        for (int x = myPosition.getColumn()+1; x <= 8; x++) {
            if (!addSpace(moves, this.myPosition.getRow(), x)) {
                break;
            }
        }
        for (int x = myPosition.getColumn()-1; x > 0; x--) {
            if (!addSpace(moves, this.myPosition.getRow(), x)) {
                break;
            }
        }
        return moves;
    }

    private Collection<ChessMove> kingMove(Collection<ChessMove> moves) {
        for (int y = myPosition.getRow()-1; y <= myPosition.getRow()+1; y++) {
            for (int x = myPosition.getColumn() - 1; x <= myPosition.getColumn() + 1; x++) {
                if (!addSpace(moves, y, x)) {
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> bishopMove(Collection<ChessMove> moves) {
        for (int i = 1; i <= 8; i++) {
            if (!addSpace(moves, myPosition.getRow()+i, myPosition.getColumn()-i)) {
                break;
            }
        }
        for (int i = 1; i <= 8; i++) {
            if (!addSpace(moves, myPosition.getRow()+i, myPosition.getColumn()+i)) {
                break;
            }
        }
        for (int i = 1; i <= 8; i++) {
            if (!addSpace(moves, myPosition.getRow()-i, myPosition.getColumn()-i)) {
                break;
            }
        }
        for (int i = 1; i <= 8; i++) {
            if (!addSpace(moves, myPosition.getRow()-i, myPosition.getColumn()+i)) {
                break;
            }
        }
        return moves;
    }

    private Collection<ChessMove> knightMove(Collection<ChessMove> moves) {
        for (int y = -2; y <= 2; y+=4) {
            for (int x = -1; x <= 1; x+=2) {
                addSpace(moves, y+myPosition.getRow(), x+myPosition.getColumn());
            }
        }
        for (int x = -2; x <= 2; x+=4) {
            for (int y = -1; y <= 1; y+=2) {
                addSpace(moves, y+myPosition.getRow(), x+myPosition.getColumn());
            }
        }
        return moves;
    }


    private Collection<ChessMove> pawnMove(Collection<ChessMove> moves) {
        if (pieceColor.equals(ChessGame.TeamColor.WHITE)) {
            if (addPawnSpace(moves, myPosition.getRow() + 1, myPosition.getColumn(), true)) {
                if (myPosition.getRow() == 2) {
                    addPawnSpace(moves, myPosition.getRow() + 2, myPosition.getColumn(), true);
                }
            }
            addPawnSpace(moves, myPosition.getRow() + 1, myPosition.getColumn() - 1, false);
            addPawnSpace(moves, myPosition.getRow() + 1, myPosition.getColumn() + 1, false);

        } else {
            if (addPawnSpace(moves, myPosition.getRow() - 1, myPosition.getColumn(), true)) {
                if (myPosition.getRow() == 7) {
                    addPawnSpace(moves, myPosition.getRow() - 2, myPosition.getColumn(), true);
                }
            }
            addPawnSpace(moves, myPosition.getRow() - 1, myPosition.getColumn() - 1, false);
            addPawnSpace(moves, myPosition.getRow() - 1, myPosition.getColumn() + 1, false);

        }
        return moves;
    }
}