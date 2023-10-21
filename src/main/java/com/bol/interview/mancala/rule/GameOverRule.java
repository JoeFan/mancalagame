package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class GameOverRule implements GameRule {
    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result) {
        if (mancalaGame.isGameOver()) {
            mancalaGame.getActiveBoardSegment().moveAllPits2House();
            mancalaGame.getInactiveBoardSegment().moveAllPits2House();
            return true;
        }
        return false;
    }
}
