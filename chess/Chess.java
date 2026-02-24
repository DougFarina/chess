package chess;

import java.util.ArrayList;

import java.lang.Math;

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

		ReturnPiece movingPiece = findPieceAt(parsed.from.file, parsed.from.rank);
		if (movingPiece == null) {
			rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return rp;
		}
		if (turn == Player.white && !isWhite(movingPiece)) {
			rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return rp;
		}

		if (turn == Player.black && !isBlack(movingPiece)) {
			rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return rp;
		}

		ReturnPiece targetPiece = findPieceAt(parsed.to.file, parsed.to.rank);
		if (sameColor(movingPiece, targetPiece)) {
			rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return rp;
		}

		// Placeholder: parsing + basic source/turn/destination validation are
		// implemented,
		// but move execution is not yet.
		// Move Execution: Should be moved to separate methods
		switch (pieceType(movingPiece)) {
			case 'P':
				if (targetPiece == null) {// Pawn movement with no target piece
					// If there is no target piece, a pawn can only move to the same file position
					// and only a maximum distance of 2
					if ((movingPiece.pieceFile != parsed.to.file)
							|| Math.abs(parsed.to.rank - movingPiece.pieceRank) > 2) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
					// Will move pawn two spaces only if it's on its starting rank
					if (Math.abs(parsed.to.rank - movingPiece.pieceRank) == 2) { // Checks if pawn wants to move 2
																					// spaces
						if (movingPiece.pieceRank == 2 || movingPiece.pieceRank == 7) {
							for (ReturnPiece p : rp.piecesOnBoard) {
								if (movingPiece.equals(p)) {
									p.pieceFile = parsed.to.file;
									p.pieceRank = parsed.to.rank;
								}
							}
							rp.message = null;
							return rp;
						}
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
					// will move pawn one space up
					if (Math.abs(parsed.to.rank - movingPiece.pieceRank) == 1) { // Checks if pawn wants to move 2
																					// spaces
						for (ReturnPiece p : rp.piecesOnBoard) {
							if (movingPiece.equals(p)) {
								p.pieceFile = parsed.to.file;
								p.pieceRank = parsed.to.rank;
							}
						}
					} 
				} /*
					 * else {
					 * char targetFile = Integer.("" + targetPiece.pieceFile);
					 * String movingFile = "" + movingPiece.pieceFile;
					 * if (targetPiece.pieceRank == movingPiece.pieceRank + 1 ||
					 * ((char)targetPiece.pieceFile - (char)movingPiece.pieceFile) == 1) {
					 * 
					 * }
					 * }
					 */

				break;
			case 'R':
				boolean sameFile = movingPiece.pieceFile == parsed.to.file; // this is basically just making sure the
																			// rook is moving horizontal
				boolean sameRank = movingPiece.pieceRank == parsed.to.rank; // same thign as ^ but for vertical

				if (!sameFile && !sameRank) {
					rp.message = ReturnPlay.Message.ILLEGAL_MOVE; // if the rook tries to move in a diagonal move it
																	// will print the illegal move message
					return rp;
				}

				if (sameFile) { //vertical move check
					int step;
					if (parsed.to.rank > movingPiece.pieceRank) {
						step = 1;
					} else {
						step = -1;
					}												//this will stop before you get to target or hit something
					for (int rank = movingPiece.pieceRank + step; rank != parsed.to.rank; rank += step) {
						if (findPieceAt(movingPiece.pieceFile, rank) != null) {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}
					}
					} else { //horizontal move check
						char fromFile = movingPiece.pieceFile.toString().charAt(0);
						char toFile = parsed.to.file.toString().charAt(0);
						int step;
						if (toFile > fromFile) {
							step = 1;
						} else {
							step = -1;
						}
						for (char fileChar = (char) (fromFile + step); fileChar != toFile; fileChar += step) {
							ReturnPiece.PieceFile file = ReturnPiece.PieceFile.valueOf(String.valueOf(fileChar)); //same logic as vertical but I had to make fromFile a char since its an enum unlike rank
							if (findPieceAt(file, movingPiece.pieceRank) != null) {
								rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
								return rp;
							}
					}
				}

				// Capture if destination has an opponent piece. I already made sure that this wont work if the attacked piece is same color
				if (targetPiece != null) {
					rp.piecesOnBoard.remove(targetPiece); //remove is an ArrayList method and I think it works fine
				}

				for (ReturnPiece p : rp.piecesOnBoard) { //this actually moves the piece on the boar
					if (movingPiece.equals(p)) {
						p.pieceFile = parsed.to.file;
						p.pieceRank = parsed.to.rank;
					}
				}
				break;
			case 'N':
				break;
			case 'B':
				break;
			case 'Q':
				break;
			case 'K':
				break;
			default:
				break;
		}

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

			addPiece(ReturnPiece.PieceType.WP, file, 2); // White Pawns

			addPiece(ReturnPiece.PieceType.BP, file, 7); // Black Pawns
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

		move.from = parseSquare(tokens[idx++]); // if both of these are null then move is invalid
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

	private static ReturnPiece findPieceAt(ReturnPiece.PieceFile file, int rank) {
		for (ReturnPiece p : rp.piecesOnBoard) {
			if (p.pieceFile == file && p.pieceRank == rank) {
				return p;
			}
		}
		return null;
	}

	private static boolean isWhite(ReturnPiece p) { // this is basically going to go to the actual piece passed in and
													// check its first char to see if it is white or black
		return p != null && p.pieceType.toString().charAt(0) == 'W';
	}

	private static boolean isBlack(ReturnPiece p) {
		return p != null && p.pieceType.toString().charAt(0) == 'B';
	}

	private static boolean sameColor(ReturnPiece a, ReturnPiece b) {
		if (a == null || b == null) {
			return false;
		}
		return isWhite(a) == isWhite(b);
	}

	private static char pieceType(ReturnPiece p) {
		String s = "" + p.pieceType;
		return s.charAt(1);
	}

}
