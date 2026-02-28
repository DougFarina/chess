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
	private static boolean whiteKingMoved;
	private static boolean blackKingMoved;
	private static boolean whiteKingsideRookMoved;
	private static boolean whiteQueensideRookMoved;
	private static boolean blackKingsideRookMoved;
	private static boolean blackQueensideRookMoved;

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
			if (turn == Player.white) {
				rp.message = ReturnPlay.Message.RESIGN_BLACK_WINS;
			} else {
				rp.message = ReturnPlay.Message.RESIGN_WHITE_WINS;
			}
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

		// Parsing and basic validation are implemented below.
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
					if (Math.abs(parsed.to.rank - movingPiece.pieceRank) == 2) { // Pawn wants to move two spaces.
						if (movingPiece.pieceRank == 2 || movingPiece.pieceRank == 7) { // Pawn is on its starting rank.
							movePiece(movingPiece, parsed.to.file, parsed.to.rank); // Move the pawn.
							handlePawnPromotion(movingPiece, parsed); // Promote if it reaches the end of the board.
						} else {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}

					}
					// will move pawn one space up
					if (Math.abs(parsed.to.rank - movingPiece.pieceRank) == 1) { // Pawn wants to move one space.
						movePiece(movingPiece, parsed.to.file, parsed.to.rank);
						handlePawnPromotion(movingPiece, parsed);
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

				movePiece(movingPiece, parsed.to.file, parsed.to.rank);
				break;
			case 'N':
				// Knights can jump over pieces so no path check is needed.
				int fileDistance = Math
						.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(parsed.to.file));
				int rankDistance = Math.abs(movingPiece.pieceRank - parsed.to.rank);
				// Knight has to move in an L-shape: (2,1) or (1,2).
				if (!((fileDistance == 2 && rankDistance == 1) || (fileDistance == 1 && rankDistance == 2))) {
					rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
					return rp;
				}
				// capture if opponent is on destination
				if (targetPiece != null) {
					rp.piecesOnBoard.remove(targetPiece);
				}

				movePiece(movingPiece, parsed.to.file, parsed.to.rank);
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
				// Check if obstacle in path
				boolean isSomethingInPath = diagonalCheck(movingPiece, parsed.to.rank);

				if (!(isSomethingInPath)) {
					rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
					return rp;
				}

				movePiece(movingPiece, parsed.to.file, parsed.to.rank);
				break;
			case 'Q':
				boolean sameFileQueen = movingPiece.pieceFile == parsed.to.file;
				boolean sameRankQueen = movingPiece.pieceRank == parsed.to.rank;

				if (sameFileQueen || sameRankQueen) {
					if (sameFileQueen) {
						if (!verticalCheck(sameFileQueen, movingPiece, parsed)) {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}
					} else {
						if (!horizontalCheck(sameRankQueen, movingPiece, parsed)) {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}
					}
				} else {
					if (targetPiece != null) {
						if (!isTargetOnDiagonal(movingPiece, targetPiece)) {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}
					} else {
						if (!isDestinationOnDiagonal(movingPiece, parsed.to)) {
							rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
							return rp;
						}
					}

					boolean isSomethingInQueenPath = diagonalCheck(movingPiece, parsed.to.rank);

					if (!isSomethingInQueenPath) {
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
				}

				if (targetPiece != null) {
					rp.piecesOnBoard.remove(targetPiece);
				}

				movePiece(movingPiece, parsed.to.file, parsed.to.rank);

				break;
			case 'K':
				int kingFileDistance = Math
						.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(parsed.to.file));
				int kingRankDistance = Math.abs(movingPiece.pieceRank - parsed.to.rank);

				if (kingRankDistance == 0 && kingFileDistance == 2) { // Castling move.
					if (!castleKing(movingPiece, parsed)) { // Castling failed.
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}
				} else {
					if (kingFileDistance > 1 || kingRankDistance > 1
							|| (kingFileDistance == 0 && kingRankDistance == 0)) { // Kings move one square in any direction.
						rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
						return rp;
					}

					if (targetPiece != null) {
						rp.piecesOnBoard.remove(targetPiece);
					}

					movePiece(movingPiece, parsed.to.file, parsed.to.rank);
				}
				break;
			default:
				break;
		}
		// changePlayer();
		rp.message = null;
		return rp;
	}

	private static boolean isTargetOnDiagonal(ReturnPiece movingPiece, ReturnPiece targetPiece) {
		// Determines if the target is on a diagonal for bishops and queens.
		int rankDistance = Math.abs(movingPiece.pieceRank - targetPiece.pieceRank);
		int fileDistance = Math
				.abs(calculatePieceFile(movingPiece.pieceFile) - calculatePieceFile(targetPiece.pieceFile));
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

	private static void changePlayer() { // Changes the current player.
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
		whiteKingMoved = false;
		blackKingMoved = false;
		whiteKingsideRookMoved = false;
		whiteQueensideRookMoved = false;
		blackKingsideRookMoved = false;
		blackQueensideRookMoved = false;

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

		move.from = parseSquare(tokens[idx++]); // If either square is null, the move is invalid.
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

	private static boolean diagonalCheck(ReturnPiece movingPiece, int toRank) {
		int iterations = toRank - movingPiece.pieceRank;
		for (int i = 1; i < iterations; i++) {
			for (ReturnPiece p : rp.piecesOnBoard) {
				if (p.pieceRank == movingPiece.pieceRank + i
						&& calculatePieceFile(p.pieceFile) == calculatePieceFile(movingPiece.pieceFile) + 1) {
					return false;
				}
			}
		}
		return true;
	}

	private static void handlePawnPromotion(ReturnPiece pawn, Move parsed) {
		if (pawn == null) {
			return;
		}

		if (pieceType(pawn) != 'P') { // Non-pawns cannot be promoted.
			return;
		}

		if (isWhite(pawn)) {
			if (pawn.pieceRank != 8) { // White pawns promote only on rank 8.
				return;
			}
		} else { // Black pawns promote only on rank 1.
			if (pawn.pieceRank != 1) {
				return;
			}
		}

		char promotionPiece;
		if (parsed.promotion == null) {
			promotionPiece = 'Q';
		} else {
			promotionPiece = parsed.promotion;
		}

		if (isWhite(pawn)) { // Promote to the chosen white piece.
			if (promotionPiece == 'Q') {
				pawn.pieceType = ReturnPiece.PieceType.WQ;
			} else if (promotionPiece == 'R') {
				pawn.pieceType = ReturnPiece.PieceType.WR;
			} else if (promotionPiece == 'B') {
				pawn.pieceType = ReturnPiece.PieceType.WB;
			} else if (promotionPiece == 'N') {
				pawn.pieceType = ReturnPiece.PieceType.WN;
			}
		} else {
			if (promotionPiece == 'Q') {
				pawn.pieceType = ReturnPiece.PieceType.BQ;
			} else if (promotionPiece == 'R') {
				pawn.pieceType = ReturnPiece.PieceType.BR;
			} else if (promotionPiece == 'B') {
				pawn.pieceType = ReturnPiece.PieceType.BB;
			} else if (promotionPiece == 'N') {
				pawn.pieceType = ReturnPiece.PieceType.BN;
			}
		}
	}

	private static void movePiece(ReturnPiece piece, ReturnPiece.PieceFile toFile, int toRank) {
		// Assumes the move is legal and only updates piece state.
		markPieceAsMoved(piece);
		piece.pieceFile = toFile;
		piece.pieceRank = toRank;
	}

	private static boolean castleKing(ReturnPiece king, Move parsed) {
		// Assumes the move is a castling attempt and tries to execute it.
		if (hasKingMoved(king) || findPieceAt(parsed.to.file, parsed.to.rank) != null) { // Reject if the king moved or the destination is occupied.
			return false;
		}

		int rank = king.pieceRank; // King and rook must stay on the same rank.
		boolean kingside = calculatePieceFile(parsed.to.file) > calculatePieceFile(king.pieceFile); // True for kingside castling, false for queenside.
		ReturnPiece.PieceFile rookFile;
		if (kingside) { // Kingside uses the h-file rook; queenside uses the a-file rook.
			rookFile = ReturnPiece.PieceFile.h;
		} else {
			rookFile = ReturnPiece.PieceFile.a;
		}
		ReturnPiece rook = findPieceAt(rookFile, rank);

		if (rook == null || pieceType(rook) != 'R' || sameColor(king, rook) == false || hasRookMoved(rook)) {
			return false;
		}

		if (!isPathClear(king.pieceFile, rank, rook.pieceFile, rook.pieceRank)) {
			return false;
		}

		Player defendingPlayer;
		if (isWhite(king)) {
			defendingPlayer = Player.white;
		} else {
			defendingPlayer = Player.black;
		}
		if (isSquareUnderAttack(king.pieceFile, rank, defendingPlayer)) {
			return false;
		}

		ReturnPiece.PieceFile middleFile;
		if (kingside) {
			middleFile = ReturnPiece.PieceFile.f;
		} else {
			middleFile = ReturnPiece.PieceFile.d;
		}
		if (isSquareUnderAttack(middleFile, rank, defendingPlayer)
				|| isSquareUnderAttack(parsed.to.file, rank, defendingPlayer)) {
			return false;
		}

		movePiece(king, parsed.to.file, rank);
		movePiece(rook, middleFile, rank);
		return true;
	}

	private static void markPieceAsMoved(ReturnPiece piece) {
		// Tracks king and rook movement for castling rights.
		if (piece == null) {
			return;
		}

		switch (piece.pieceType) {
			case WK:
				whiteKingMoved = true;
				break;
			case BK:
				blackKingMoved = true;
				break;
			case WR:
				if (piece.pieceRank == 1) {
					if (piece.pieceFile == ReturnPiece.PieceFile.a) {
						whiteQueensideRookMoved = true;
					} else if (piece.pieceFile == ReturnPiece.PieceFile.h) {
						whiteKingsideRookMoved = true;
					}
				}
				break;
			case BR:
				if (piece.pieceRank == 8) {
					if (piece.pieceFile == ReturnPiece.PieceFile.a) {
						blackQueensideRookMoved = true;
					} else if (piece.pieceFile == ReturnPiece.PieceFile.h) {
						blackKingsideRookMoved = true;
					}
				}
				break;
			default:
				break;
		}
	}

	private static boolean hasKingMoved(ReturnPiece king) {
		if (king.pieceType == ReturnPiece.PieceType.WK) {
			return whiteKingMoved;
		}
		if (king.pieceType == ReturnPiece.PieceType.BK) {
			return blackKingMoved;
		}
		return true;
	}

	private static boolean hasRookMoved(ReturnPiece rook) {
		if (rook.pieceType == ReturnPiece.PieceType.WR && rook.pieceRank == 1) {
			if (rook.pieceFile == ReturnPiece.PieceFile.a) {
				return whiteQueensideRookMoved;
			}
			if (rook.pieceFile == ReturnPiece.PieceFile.h) {
				return whiteKingsideRookMoved;
			}
		}
		if (rook.pieceType == ReturnPiece.PieceType.BR && rook.pieceRank == 8) {
			if (rook.pieceFile == ReturnPiece.PieceFile.a) {
				return blackQueensideRookMoved;
			}
			if (rook.pieceFile == ReturnPiece.PieceFile.h) {
				return blackKingsideRookMoved;
			}
		}
		return true;
	}

	private static boolean isSquareUnderAttack(ReturnPiece.PieceFile targetFile, int targetRank,
			Player defendingPlayer) {
		for (ReturnPiece piece : rp.piecesOnBoard) {
			if ((defendingPlayer == Player.white && isBlack(piece))
					|| (defendingPlayer == Player.black && isWhite(piece))) {
				if (canAttackSquare(piece, targetFile, targetRank)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean canAttackSquare(ReturnPiece attacker, ReturnPiece.PieceFile targetFile, int targetRank) {
		int fileDistance = Math.abs(calculatePieceFile(attacker.pieceFile) - calculatePieceFile(targetFile));
		int rankDistance = Math.abs(attacker.pieceRank - targetRank);

		switch (pieceType(attacker)) {
			case 'P':
				int direction;
				if (isWhite(attacker)) {
					direction = 1;
				} else {
					direction = -1;
				}
				return rankDistance == 1
						&& targetRank - attacker.pieceRank == direction
						&& fileDistance == 1;
			case 'N':
				return (fileDistance == 2 && rankDistance == 1) || (fileDistance == 1 && rankDistance == 2);
			case 'B':
				return fileDistance == rankDistance
						&& fileDistance != 0
						&& isPathClear(attacker.pieceFile, attacker.pieceRank, targetFile, targetRank);
			case 'R':
				return (attacker.pieceFile == targetFile || attacker.pieceRank == targetRank)
						&& !(attacker.pieceFile == targetFile && attacker.pieceRank == targetRank)
						&& isPathClear(attacker.pieceFile, attacker.pieceRank, targetFile, targetRank);
			case 'Q':
				return (((attacker.pieceFile == targetFile || attacker.pieceRank == targetRank)
						|| (fileDistance == rankDistance))
						&& !(attacker.pieceFile == targetFile && attacker.pieceRank == targetRank)
						&& isPathClear(attacker.pieceFile, attacker.pieceRank, targetFile, targetRank));
			case 'K':
				return fileDistance <= 1 && rankDistance <= 1 && (fileDistance != 0 || rankDistance != 0);
			default:
				return false;
		}
	}

	private static boolean isPathClear(ReturnPiece.PieceFile fromFile, int fromRank, ReturnPiece.PieceFile toFile, int toRank) {
		int fromFileValue = calculatePieceFile(fromFile);
		int toFileValue = calculatePieceFile(toFile);
		int fileDiff = toFileValue - fromFileValue;
		int rankDiff = toRank - fromRank;

		int fileStep = Integer.compare(fileDiff, 0);
		int rankStep = Integer.compare(rankDiff, 0);

		if (!((fileDiff == 0 && rankDiff != 0)
				|| (rankDiff == 0 && fileDiff != 0)
				|| (Math.abs(fileDiff) == Math.abs(rankDiff) && fileDiff != 0))) {
			return false;
		}

		int currentFile = fromFileValue + fileStep;
		int currentRank = fromRank + rankStep;

		while (currentFile != toFileValue || currentRank != toRank) {
			ReturnPiece.PieceFile currentPieceFile = ReturnPiece.PieceFile.values()[currentFile - 10];
			if (findPieceAt(currentPieceFile, currentRank) != null) {
				return false;
			}
			currentFile += fileStep;
			currentRank += rankStep;
		}

		return true;
	}

}
