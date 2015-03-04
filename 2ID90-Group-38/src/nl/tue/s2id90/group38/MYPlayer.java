package nl.tue.s2id90.group38;


import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.Move;

/**
 *
 * @author s116880 and s122041
 */
public class MYPlayer extends DraughtsPlayer {
    
    //Multipliers to increase the score for being in a certain spot, when the current player is white
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
    //Multipliers to increase the score for being in a certain spot, when the current player is black
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
    
    //selected multiplier array.
    double[] boardMultipliers;
    
    //the best evaluation in a round
    int bestEvaluation;
    
    //to determine whether the pieces are black or white
    boolean isWhite;
    
    //stop sign
    boolean stop = false;
    
    //the value to be returned
    int value;
    
    //the value of how much it worths
    int piece = 10, king = 20, enemyPiece = -7, enemyKing = -14;
    
    public MYPlayer() {
        super(MYPlayer.class.getResource("resources/optimist.png"));
    }
    
    //return the best move
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
            try {
                value = alphaBetaMax(node, min, max, 0, depth);
            } catch (AIStoppedException ex) {
                Logger.getLogger(MYPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
            depth++;
        }
        
        //return a random move
        if (node.getBestMove() == null || !s.getMoves().contains(node.getBestMove())) {
            List<Move> moves = s.getMoves();
            Collections.shuffle(moves);
            node.setBestMove(moves.get(0));
        }
        return node.getBestMove();
    }
    
    //the alpha-beta maximum search 
    public int alphaBetaMax(GameNode node, int alpha, int beta, int depth, int maxDepth) throws AIStoppedException{
        DraughtsState state = node.getState();
        if (depth >= maxDepth) {
            return evaluate(state, true);
        }
        if(stop) {
          stop = false;
          throw new AIStoppedException ( ) ;
        }
        List<Move> moves = state.getMoves();
        for (Move move : moves) {
            if(stop) {
               stop = false;
               throw new AIStoppedException ( ) ;
            }
            
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
    
    //the alpha-beta minimum search 
    public int alphaBetaMin(GameNode node, int alpha, int beta, int depth, int maxDepth) throws AIStoppedException {
        DraughtsState state = node.getState();
        if (depth >= maxDepth) {
            return -evaluate(state, false);
        }
        List<Move> moves = state.getMoves();
        for (Move move : moves) {
            if(stop) {
               stop = false;
               throw new AIStoppedException ( ) ;
            }
           
            state.doMove(move);
            beta = (int) Math.min(beta, alphaBetaMax(node, alpha, beta, depth + 1, maxDepth));
            state.undoMove(move);
            if (beta <= alpha) {
                return alpha;
            }
        }
        return beta;
    }
    
    //the evaluation function
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
    
    //Gamenode to represent a game state and set the best move
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
    }
    
    //return the value
    @Override
    public Integer getValue() {
        return value;
    }

    //set the stop to true
    @Override
    public void stop() {
        stop = true;
    }
    
    //throws exception to stop the alpha-beta search
    public class AIStoppedException extends Exception {
        public AIStoppedException() {
            
        }
    }
}


