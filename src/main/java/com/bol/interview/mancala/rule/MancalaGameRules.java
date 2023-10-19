package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

import java.util.Arrays;

public class MancalaGameRules {

    private static GameRule[] rules = new GameRule[]{
            new MoveBothPits2PlayerHouseRule(),
            new AnotherRoundRule(),
            new SwitchTurnRule()
    };
    public static boolean applyRules(MancalaGame mancalaGame, SegmentSowResult segmentSowResult){
        if(segmentSowResult == null){
            return false;
        }
        return Arrays.stream(rules).filter(gameRule -> gameRule.apply(mancalaGame, segmentSowResult)).findFirst().isPresent();
    }
}
