package chess;

import java.util.ArrayList;

import chess.ReturnPiece.PieceFile;

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
						} else {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}

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
				}
				/*
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
				// Make sure the rook is moving in a straight line (not diagonal).
				boolean sameFileBishop = movingPiece.pieceFile == parsed.to.file;
				boolean sameRankBishop = movingPiece.pieceRank == parsed.to.rank;

				if (!sameFileBishop && !sameRankBishop) {
					// Rooks cannot move diagonally.
					rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
					return rp;
				}

				if (sameFileBishop) { // vertical move check
					if (!verticalCheck(sameFileBishop, movingPiece, parsed)) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
				} else { // horizontal move check
					if (!horizontalCheck(sameRankBishop, movingPiece, parsed)) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
				}

				// Capture if destination has an opponent piece. I already made sure that this
				// wont work if the attacked piece is same color
				if (targetPiece != null) {
					rp.piecesOnBoard.remove(targetPiece); // Remove captured piece.
				}

				// Move the rook on the board.
				for (ReturnPiece p : rp.piecesOnBoard) {
					if (movingPiece.equals(p)) {
						p.pieceFile = parsed.to.file;
						p.pieceRank = parsed.to.rank;
					}
				}
				break;
			case 'N':
				// Knights can jump over pieces so no path check is needed.
				int fileDistance = Math.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(parsed.to.file));
				int rankDistance = Math.abs(movingPiece.pieceRank - parsed.to.rank);
				// Knight has to  move in an L-shape: (2,1) or (1,2).
				if (!((fileDistance == 2 && rankDistance == 1) || (fileDistance == 1 && rankDistance == 2))) {
					rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
					return rp;
				}
				// capture if opponent is on destination
				if (targetPiece != null) {
					rp.piecesOnBoard.remove(targetPiece);
				}

				// actually move the knight
				for (ReturnPiece p : rp.piecesOnBoard) {
					if (movingPiece.equals(p)) {
						p.pieceFile = parsed.to.file;
						p.pieceRank = parsed.to.rank;
					}
				}
				break;
			case 'B':
				if (targetPiece != null) {
					if (!isTargetOnDiagonal(movingPiece, targetPiece)) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
					rp.piecesOnBoard.remove(targetPiece);
				} else {
					if (!isDestinationOnDiagonal(movingPiece, parsed.to)) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
				}
				//Check if obstacle in path
				boolean isSomethingInPath = diagonalCheck(movingPiece, parsed.to);
				System.out.println(isSomethingInPath);

				if(!(isSomethingInPath)){
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
				}
				

				for (ReturnPiece p : rp.piecesOnBoard) {
					if (movingPiece.equals(p)) {
						p.pieceFile = parsed.to.file;
						p.pieceRank = parsed.to.rank;
					}
				}
				break;
			case 'Q':
				// Queen moves like a rook and a bishop combined
				boolean sameFileQueen = movingPiece.pieceFile == parsed.to.file;
				boolean sameRankQueen = movingPiece.pieceRank == parsed.to.rank;

				boolean isMoveDiagonal = isTargetOnDiagonal(movingPiece, targetPiece);

				break;
			case 'K':
				break;
			default:
				break;
		}
		//changePlayer();
		rp.message = null;
		return rp;
	}

	private static boolean isTargetOnDiagonal(ReturnPiece movingPiece, ReturnPiece targetPiece) {
		// Determines if target is on a diagonal (for bishop and queen).
		// TODO Auto-generated method stub
		int rankDistance = Math.abs(movingPiece.pieceRank - targetPiece.pieceRank);
		int fileDistance = Math.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(targetPiece.pieceFile));
		if (rankDistance == fileDistance) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isDestinationOnDiagonal(ReturnPiece movingPiece, Square destination) {
		// Determines if destination is on a diagonal (for bishop and queen).
		int rankDistance = Math.abs(movingPiece.pieceRank - destination.rank);
		int fileDistance = Math.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(destination.file));
		if (rankDistance == fileDistance) {
			return true;
		} else {
			return false;
		}
	}

	private static void changePlayer() { // Changes Player color
		// TODO Auto-generated method stub
		if (turn == Player.white) {
			turn = Player.black;
		} else {
			turn = Player.white;
		}
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

	private static boolean isWhite(ReturnPiece p) {
		// Check first char of piece type to determine color.
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

	private static int calculatePieceFile(ReturnPiece.PieceFile p) {
		String s = "" + p;
		char c = s.charAt(0);
		int i = Character.getNumericValue(c);
		return i;
	}

	private static boolean verticalCheck(boolean sameFile, ReturnPiece movingPiece, Move parsed) {
		int step;
		if (parsed.to.rank > movingPiece.pieceRank) {
			step = 1;
		} else {
			step = -1;
		} // This will stop before the target square.
		for (int rank = movingPiece.pieceRank + step; rank != parsed.to.rank; rank += step) {
			if (findPieceAt(movingPiece.pieceFile, rank) != null) {
				return false;
			}
		}
		return true;
	}

	private static boolean horizontalCheck(boolean sameRank, ReturnPiece movingPiece, Move parsed) {
		int fromFile = calculatePieceFile(movingPiece.pieceFile);
		int toFile = calculatePieceFile(parsed.to.file);
		int step;
		if (toFile > fromFile) {
			step = 1;
		} else {
			step = -1;
		}
		for (int fileNum = fromFile + step; fileNum != toFile; fileNum += step) {
			// Convert the file number back to a file enum (a-h are 10-17 here).
			ReturnPiece.PieceFile file = ReturnPiece.PieceFile.values()[fileNum - 10];
				if (findPieceAt(file, movingPiece.pieceRank) != null) {
					return false;
				}
			}
		return true;
	}

	private static boolean diagonalCheck(ReturnPiece movingPiece, Square destination) {
		int horizontalStep, verticalStep;
		if (destination.rank > movingPiece.pieceRank) {
			verticalStep = 1;
		} else {
			verticalStep = -1;
		}
		if (calculatePieceFile(destination.file) > calculatePieceFile(movingPiece.pieceFile)) {
			horizontalStep = 1;
		} else {
			horizontalStep = -1;
		}

		int iterations = Math.abs(destination.rank - movingPiece.pieceRank);
		for(int i = 1; i < iterations; i++) {
			int checkFile = calculatePieceFile(movingPiece.pieceFile) + horizontalStep*i;
			int checkRank = movingPiece.pieceRank + verticalStep*i;
			for(ReturnPiece p: rp.piecesOnBoard) {
				if(calculatePieceFile(p.pieceFile)==checkFile && p.pieceRank==checkRank) {
					return false;
				}
			}
		}
		return true;
	}

}
