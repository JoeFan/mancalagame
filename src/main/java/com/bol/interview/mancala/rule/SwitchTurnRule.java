package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class SwitchTurnRule implements GameRule {


    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result) {

        mancalaGame.switchTurn();
        return true;
    }
}
