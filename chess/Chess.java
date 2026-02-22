package chess;

import java.util.ArrayList;

public class Chess {

	enum Player {
		white, black
	}

	private static Player turn;
	public static ReturnPlay rp = new ReturnPlay();

	private static class Square {
		ReturnPiece.PieceFile file;
		int rank;
	}

	private static class Move {
		Square from;
		Square to;
		Character promotion;
		boolean resign;
		boolean drawOffer;
		boolean valid;
	}

	/**
	 * Plays the next move for whichever player has the turn.
	 *
	 * @param move String for next move, e.g. "a2 a3"
	 *
	 * @return A ReturnPlay instance that contains the result of the move.
	 */
	public static ReturnPlay play(String move) {
		Move parsed = parseMove(move);
		if (parsed == null || !parsed.valid) {
			rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return rp;
		}

		if (parsed.resign) {
			rp.message = (turn == Player.white)
					? ReturnPlay.Message.RESIGN_BLACK_WINS
					: ReturnPlay.Message.RESIGN_WHITE_WINS;
			return rp;
		}

		// Placeholder: move parsing is implemented, but move execution is not yet.
		rp.message = null;
		return rp;
	}

	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		turn = Player.white; // White always starts first
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

	private static Move parseMove(String rawMove) {
		if (rawMove == null) {
			return null;
		}

		String trimmed = rawMove.trim();
		if (trimmed.isEmpty()) {
			return invalidMove();
		}

		Move move = new Move();

		if (trimmed.equals("resign")) {
			move.resign = true;
			move.valid = true;
			return move;
		}

		String[] tokens = trimmed.split("\\s+");
		int idx = 0;

		if (tokens.length < 2) {
			return invalidMove();
		}

		move.from = parseSquare(tokens[idx++]);
		move.to = parseSquare(tokens[idx++]);
		if (move.from == null || move.to == null) {
			return invalidMove();
		}

		if (idx < tokens.length && isPromotionToken(tokens[idx])) {
			move.promotion = Character.toUpperCase(tokens[idx].charAt(0));
			idx++;
		}

		if (idx < tokens.length && tokens[idx].equals("draw?")) {
			move.drawOffer = true;
			idx++;
		}

		if (idx != tokens.length) {
			return invalidMove();
		}

		move.valid = true;
		return move;
	}

	private static Move invalidMove() {
		Move move = new Move();
		move.valid = false;
		return move;
	}

	private static Square parseSquare(String token) {
		if (token == null || token.length() != 2) {
			return null;
		}

		char fileChar = Character.toLowerCase(token.charAt(0));
		char rankChar = token.charAt(1);

		if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
			return null;
		}

		Square square = new Square();
		square.file = ReturnPiece.PieceFile.valueOf(String.valueOf(fileChar));
		square.rank = rankChar - '0';
		return square;
	}

	private static boolean isPromotionToken(String token) {
		if (token == null || token.length() != 1) {
			return false;
		}
		char c = Character.toUpperCase(token.charAt(0));
		return c == 'Q' || c == 'R' || c == 'B' || c == 'N';
	}
}
