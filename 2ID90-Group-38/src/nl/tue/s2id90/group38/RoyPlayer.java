/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group38;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import nl.tue.s2id90.contest.CompetitionGUI;
import nl.tue.s2id90.contest.util.TimedSearchTask;
import nl.tue.s2id90.draughts.DraughtsCompetitionGUI;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import org10x10.dam.game.BoardState;
import org10x10.dam.game.Move;
import org10x10.dam.game.MoveGenerator;
import org10x10.dam.game.MoveGeneratorFactory;

/**
 *
 * @author Roy
 */
public class RoyPlayer extends DraughtsPlayer {

    //Multipliers to increase the score for being in a certain spot, when the current
    //player is white
    final float[] whiteBoardValues
            = {
                1.20f, 1.20f, 1.20f, 1.20f, 1.20f,
                1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
                1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.35f, 1.00f, 1.30f,
                1.30f, 1.00f, 1.35f, 1.35f, 1.00f,
                1.00f, 1.30f, 1.30f, 1.30f, 1.10f,
                1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
                1.20f, 1.20f, 1.20f, 1.20f, 1.20f
            };
    //Multipliers to increase the score for being in a certain spot, when the current
    //player is black
    final float[] blackBoardValues
            = {
                1.20f, 1.20f, 1.20f, 1.20f, 1.20f,
                1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
                1.10f, 1.30f, 1.30f, 1.30f, 1.00f,
                1.00f, 1.35f, 1.35f, 1.00f, 1.30f,
                1.30f, 1.00f, 1.35f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
                1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
                1.20f, 1.20f, 1.20f, 1.20f, 1.20f
            };
    /* different corners
     1.20f, 1.20f, 1.20f, 1.20f, 1.20f,
     1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
     1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
     1.10f, 1.30f, 1.30f, 1.30f, 1.00f,
     1.00f, 1.35f, 1.35f, 1.00f, 1.25f,
     1.20f, 1.00f, 1.35f, 1.00f, 1.00f,
     1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
     1.10f, 1.00f, 1.00f, 1.00f, 1.00f,
     1.00f, 1.00f, 1.00f, 1.00f, 1.10f,
     1.20f, 1.20f, 1.20f, 1.20f, 1.20f


     */
    //the current selected multiplier array.
    float[] boardValues;
    //the best move this round
    Move bestMove;
    //the best score this round
    int bestScore;
    //whether we are black or white
    boolean isWhite;
    //whether we have to stop or not
    boolean stop = false;
    //the best score, to be returned to the GUI tool
    int value;
    //the number of nodes we have visited
    int numNodes = 0;
    //how much every piece is worth
    int piece = 10, king = 20, enemyPiece = -7, enemyKing = -14;

    public RoyPlayer() {
        super(RoyPlayer.class
                .getResource("resources/optimist.png"));
    }

    /**
     * @param s
     * @return a random move *
     */
    @Override
    public Move getMove(DraughtsState s) {
        
        //Set the multiplier array to the correct one
        if (s.isWhiteToMove()) {
            boardValues = whiteBoardValues;
        } else {
            boardValues = blackBoardValues;
        }
        //reset the number of visited nodes
        numNodes = 0;
        //set the score to the minimum possible score
        bestScore = Integer.MIN_VALUE;
        //reset the best move
        bestMove = null;
        //reset the stop flag
        stop = false;
        //whether we are black or white
        isWhite = s.isWhiteToMove();
        //the current depth in the state tree
        int level = 1;
        //the maximum level we will search in this tree
        int maxLevel = 3;
        //the minimum cutoff value (a in betaMin)
        int min = Integer.MIN_VALUE;
        //the maximum cutoff value (b in betaMax)
        int max = Integer.MAX_VALUE;
        //use iterative deepening until we get a stop sign or we have reached
        //the maximum depth
        while (!stop) {
            bestScore = 0;
            if (level >= maxLevel) {
                break;
            }
            //get the score for the selected move
            value = alphaBetaMax(s, min, max, 0, level);
            //increase the level counter
            level++;
        }
      
        //if we have no bestMove or somehow a incorrect move is selected
        //we use this as backup (select a random move)
        if (bestMove == null || !s.getMoves().contains(bestMove)) {
            List<Move> moves = s.getMoves();
            Collections.shuffle(moves);
            bestMove = moves.get(0);
            System.out.println("doing a stupid move...");
        }
       
        //some debug prints
        System.out.println("\u001B[33mdone " + level + " \u001B[36miterations");
        System.out.println("\u001B[34minspected " + numNodes + "\u001B[36m nodes");
        System.out.println("\u001B[32mScore: " + bestScore);
        //return our move
        return bestMove;
    }

    public int alphaBetaMax(DraughtsState s, int a, int b, int depth, int maxDepth) {
        //if we have reached the maximum depth or get a stop sign
        //we calculate the score and increase the number of visited nodes by one
        if (depth >= maxDepth || stop) {
            numNodes++;
            return getScore(s, true, depth);
        }
        //get all the moves
        List<Move> moves = s.getMoves();
        //if we have only 1 move, we have to do this move so immediatly remove
        if (moves.size() == 1 && depth == 0) {
            bestMove = moves.get(0);
            bestScore = 1;
            return 1;
        }
        //check what move is the best
        for (Move move : moves) {
            //do a move and calculate it's score recursively
            s.doMove(move);
            a = (int) Math.max(a, alphaBetaMin(s, a, b, depth + 1, maxDepth));
            //undo the move to restore the state
            s.undoMove(move);
            //if the score is > bestScore and it is a possible move in the
            //current state, we update the score and the best move
            if (a > bestScore && depth == 0) {
                bestScore = a;
                bestMove = move;
            }
            //if a >= b we early return, because the other possibilities are not
            //worth looking at
            if (a >= b) {
                return b;
            }
        }
        //return the score for this state
        return a;
    }

    public int alphaBetaMin(DraughtsState s, int a, int b, int depth, int maxDepth) {
        //if we have reached the maximum depth or get a stop sign
        //we calculate the score and increase the number of visited nodes by one
        //the score is multiplied with -1, because in a zero-sum game, win for one
        //player is lose for the other player
        if (depth >= maxDepth || stop) {
            numNodes++;
            return -getScore(s, false, depth);
        }
        //get all the moves
        List<Move> moves = s.getMoves();
        //check what move is the best
        for (Move move : moves) {
            //do a move and calculate it's score recursively
            s.doMove(move);
            b = (int) Math.min(b, alphaBetaMax(s, a, b, depth + 1, maxDepth));
            //undo the move to restore the state
            s.undoMove(move);
            //if b <= a we early return, because the other possibilities are not
            //worth looking at
            if (b <= a) {
                return a;
            }
        }
        //return the score for this state
        return b;

    }

    //the state evaluation function
    public int getScore(DraughtsState s, boolean myTurn, int depth) {
        if (s.isEndState()) {
            if (myTurn) {
                return 10000;
            }
            return -10000;
        }
        //the starting score = 0
        float score = 0;
        //get an array of all the pieces
        int[] pieces = s.getPieces();
        //the first piece in the array is somehow always empty, thus we ignore it
        for (int i = 1; i < pieces.length; i++) {
            //calculate the score for piece in a cell and multiply it by it's
            //position multiplier
            score += boardValues[i - 1] * getPieceValue(pieces[i], isWhite);
        }
        //return the score
        return (int) score;
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

    //return our best score to the GUI tool
    @Override
    public Integer getValue() {
        return value;
    }

    //set the stop flag to true
    @Override
    public void stop() {
        stop = true;
    }

}
