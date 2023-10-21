package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.BoardSegment;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class SwitchTurnRule implements GameRule {


    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result) {

        BoardSegment temp = mancalaGame.getActiveBoardSegment();
        mancalaGame.setActiveBoardSegment(mancalaGame.getInactiveBoardSegment());
        mancalaGame.setInactiveBoardSegment(temp);
        return true;
    }
}
