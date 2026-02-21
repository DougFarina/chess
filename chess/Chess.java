package chess;

import java.util.ArrayList;

public class Chess {

	enum Player {
		white, black
	}

	private static Player turn;
	public static ReturnPlay rp = new ReturnPlay();

	/**
	 * Plays the next move for whichever player has the turn.
	 *
	 * @param move String for next move, e.g. "a2 a3"
	 *
	 * @return A ReturnPlay instance that contains the result of the move.
	 */
	public static ReturnPlay play(String move) {
		// Placeholder: return current board state without changing it yet.
		// (This keeps the driver from crashing while you build move logic.)
		rp.message = null;
		return rp;
	}

	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		turn = Player.white;
		rp = new ReturnPlay();
		rp.message = null;
		rp.piecesOnBoard = new ArrayList<>();

		// Pawns
		for (int i = 0; i < 8; i++) {
			ReturnPiece.PieceFile file = ReturnPiece.PieceFile.values()[i];

			ReturnPiece whitePawn = new ReturnPiece();
			whitePawn.pieceType = ReturnPiece.PieceType.WP;
			whitePawn.pieceFile = file;
			whitePawn.pieceRank = 2;
			rp.piecesOnBoard.add(whitePawn);

			ReturnPiece blackPawn = new ReturnPiece();
			blackPawn.pieceType = ReturnPiece.PieceType.BP;
			blackPawn.pieceFile = file;
			blackPawn.pieceRank = 7;
			rp.piecesOnBoard.add(blackPawn);
		}

		// White back rank (rank 1)
		addPiece(ReturnPiece.PieceType.WR, ReturnPiece.PieceFile.a, 1);
		addPiece(ReturnPiece.PieceType.WN, ReturnPiece.PieceFile.b, 1);
		addPiece(ReturnPiece.PieceType.WB, ReturnPiece.PieceFile.c, 1);
		addPiece(ReturnPiece.PieceType.WQ, ReturnPiece.PieceFile.d, 1);
		addPiece(ReturnPiece.PieceType.WK, ReturnPiece.PieceFile.e, 1);
		addPiece(ReturnPiece.PieceType.WB, ReturnPiece.PieceFile.f, 1);
		addPiece(ReturnPiece.PieceType.WN, ReturnPiece.PieceFile.g, 1);
		addPiece(ReturnPiece.PieceType.WR, ReturnPiece.PieceFile.h, 1);

		// Black back rank (rank 8)
		addPiece(ReturnPiece.PieceType.BR, ReturnPiece.PieceFile.a, 8);
		addPiece(ReturnPiece.PieceType.BN, ReturnPiece.PieceFile.b, 8);
		addPiece(ReturnPiece.PieceType.BB, ReturnPiece.PieceFile.c, 8);
		addPiece(ReturnPiece.PieceType.BQ, ReturnPiece.PieceFile.d, 8);
		addPiece(ReturnPiece.PieceType.BK, ReturnPiece.PieceFile.e, 8);
		addPiece(ReturnPiece.PieceType.BB, ReturnPiece.PieceFile.f, 8);
		addPiece(ReturnPiece.PieceType.BN, ReturnPiece.PieceFile.g, 8);
		addPiece(ReturnPiece.PieceType.BR, ReturnPiece.PieceFile.h, 8);
	}

	private static void addPiece(ReturnPiece.PieceType type, ReturnPiece.PieceFile file, int rank) {
		ReturnPiece p = new ReturnPiece();
		p.pieceType = type;
		p.pieceFile = file;
		p.pieceRank = rank;
		rp.piecesOnBoard.add(p);
	}
}
