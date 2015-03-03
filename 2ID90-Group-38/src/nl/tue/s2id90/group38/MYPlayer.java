package nl.tue.s2id90.group38;


import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 *
 * @author s116880
 */
public class MYPlayer extends DraughtsPlayer {
    
        //Multipliers to increase the score for being in a certain spot, when the current
    //player is white
    final double[] whiteMultipliers
            = {
                1.20, 1.20, 1.20, 1.20, 1.20,
                1.10, 1.00, 1.00, 1.00, 1.00,
                1.00, 1.00, 1.00, 1.00, 1.10,
                1.10, 1.00, 1.00, 1.00, 1.00,
                1.00, 1.00, 1.35, 1.00, 1.30,
                1.30, 1.00, 1.35, 1.35, 1.00,
                1.00, 1.30, 1.30, 1.30, 1.10,
                1.10, 1.00, 1.00, 1.00, 1.00,
                1.00, 1.00, 1.00, 1.00, 1.10,
                1.20, 1.20, 1.20, 1.20, 1.20
            };
    final double[] blackMultipliers
            = {
                1.20, 1.20, 1.20, 1.20, 1.20,
                1.10, 1.00, 1.00, 1.00, 1.00,
                1.00, 1.00, 1.00, 1.00, 1.10,
                1.10, 1.30, 1.30, 1.30, 1.00,
                1.00, 1.35, 1.35, 1.00, 1.30,
                1.30, 1.00, 1.35, 1.00, 1.00,
                1.00, 1.00, 1.00, 1.00, 1.10,
                1.10, 1.00, 1.00, 1.00, 1.00,
                1.00, 1.00, 1.00, 1.00, 1.10,
                1.20, 1.20, 1.20, 1.20, 1.20
            };
    
    double[] boardMultipliers;
    Move bestMove;
    int bestEvaluation;
    boolean isWhite;
    boolean stop = false;
    int value;
    int piece = 10, king = 20, enemyPiece = -7, enemyKing = -14;
    
    public MYPlayer() {
        super(MYPlayer.class.getResource("resources/optimist.png"));
    }
    @Override
    public Move getMove(DraughtsState s) {
        if (s.isWhiteToMove()) {
            boardMultipliers = whiteMultipliers;
        } else {
            boardMultipliers = blackMultipliers;
        }
        bestEvaluation = Integer.MIN_VALUE;
        bestMove = null;
        stop = false;
        isWhite = s.isWhiteToMove();
        int depth = 1;
        int maxDepth = 3;
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        while (!stop) {
            bestEvaluation = 0;
            if (depth >= maxDepth) {
                break;
            }
            value = alphaBetaMax(s, min, max, 0, depth);
            depth++;
        }
        return bestMove;
    }
    
    public int alphaBetaMax(DraughtsState s, int alpha, int beta, int depth, int maxDepth) {
    if (depth >= maxDepth) {
        return evaluate(s, true);
    }
    List<Move> moves = s.getMoves();
    for (Move move : moves) {
            s.doMove(move);
            alpha = (int) Math.max(alpha, alphaBetaMin(s, alpha, beta, depth + 1, maxDepth));
            s.undoMove(move);
            if (alpha > bestEvaluation && depth == 0) {
                bestEvaluation = alpha;
                bestMove = move;
            }
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    public int alphaBetaMin(DraughtsState s, int alpha, int beta, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            return -evaluate(s, false);
        }
        List<Move> moves = s.getMoves();
        for (Move move : moves) {
            s.doMove(move);
            beta = (int) Math.min(beta, alphaBetaMax(s, alpha, beta, depth + 1, maxDepth));
            s.undoMove(move);
            if (beta <= alpha) {
                return alpha;
            }
        }
        return beta;
    }
    
    public int evaluate(DraughtsState s, boolean maxPlayerTurn) {
        if (s.isEndState()) {
            if (maxPlayerTurn) {
                return 9999;
            }
            return -9999;
        }
        double evaluation = 0;
        int[] pieces = s.getPieces();
        for (int i = 1; i < pieces.length; i++) {
            evaluation = evaluation + boardMultipliers[i - 1] * getPieceValue(pieces[i], isWhite);
        }
        return (int) evaluation;
    }
    
    public float getPieceValue(int piece, boolean isWhite) {
        //calculate the value of a certain piece
        if (isWhite) {
            switch (piece) {
                case DraughtsState.WHITEPIECE:
                    return this.piece;
                case DraughtsState.WHITEKING:
                    return king;
                case DraughtsState.BLACKPIECE:
                    return enemyPiece;
                case DraughtsState.BLACKKING:
                    return enemyKing;
                case DraughtsState.EMPTY:
                    return 0;
            }

            System.out.println("wrong piece ");
            return -1;
        } else {
            switch (piece) {
                case DraughtsState.WHITEPIECE:
                    return enemyPiece;
                case DraughtsState.WHITEKING:
                    return enemyKing;
                case DraughtsState.BLACKPIECE:
                    return this.piece;
                case DraughtsState.BLACKKING:
                    return king;
                case DraughtsState.EMPTY:
                    return 0;
            }
            System.out.println("wrong piece ");
            return -1;
        }
    }
}


