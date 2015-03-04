package nl.tue.s2id90.group38;


import java.util.Collections;
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
        GameNode node = new GameNode(s);
        if (s.isWhiteToMove()) {
            boardMultipliers = whiteMultipliers;
        } else {
            boardMultipliers = blackMultipliers;
        }
        bestEvaluation = Integer.MIN_VALUE;
        node.setBestMove(null);
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
            value = alphaBetaMax(node, min, max, 0, depth);
            depth++;
        }
        
        //a random move
        if (node.getBestMove() == null || !s.getMoves().contains(node.getBestMove())) {
            List<Move> moves = s.getMoves();
            Collections.shuffle(moves);
            node.setBestMove(moves.get(0));
        }
        return node.getBestMove();
    }
    
    public int alphaBetaMax(GameNode node, int alpha, int beta, int depth, int maxDepth) {
        DraughtsState state = node.getState();
        if (depth >= maxDepth) {
            return evaluate(state, true);
        }
        List<Move> moves = state.getMoves();
        for (Move move : moves) {
            state.doMove(move);
            alpha = (int) Math.max(alpha, alphaBetaMin(node, alpha, beta, depth + 1, maxDepth));
            state.undoMove(move);
            if (alpha > bestEvaluation && depth == 0) {
                bestEvaluation = alpha;
                node.setBestMove(move);
            }
            if (alpha >= beta) {
                return beta;
            }
        }
        return alpha;
    }

    public int alphaBetaMin(GameNode node, int alpha, int beta, int depth, int maxDepth) {
        DraughtsState state = node.getState();
        if (depth >= maxDepth) {
            return -evaluate(state, false);
        }
        List<Move> moves = state.getMoves();
        for (Move move : moves) {
            state.doMove(move);
            beta = (int) Math.min(beta, alphaBetaMax(node, alpha, beta, depth + 1, maxDepth));
            state.undoMove(move);
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
    
    public double getPieceValue(int piece, boolean isWhite) {
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
            return -1;
        }
    }
    
    public class GameNode {
        private final DraughtsState gameState;
        private Move bestMove;
        private int value;
    
        public GameNode(DraughtsState state) {
            gameState = state;
        }
    
        public DraughtsState getState() {
            return gameState;
        }
    
        public Move getBestMove() {
            return bestMove;
        }
    
        public void setBestMove(Move move) {
            bestMove = move;
        }
    
        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}


