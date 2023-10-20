package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

import java.util.Arrays;

public class MancalaGameRules {

    //the order of the rules has impact on the result!
    private static GameRule[] rules = new GameRule[]{
            new AnotherRoundRule(),
            new MoveBothPits2PlayerHouseRule(),
            new SwitchTurnRule(),
            new GameOverRule()
    };
    public static boolean applyRules(MancalaGame mancalaGame, SegmentSowResult segmentSowResult){
        if(segmentSowResult == null){
            return false;
        }
        return Arrays.stream(rules).filter(gameRule -> gameRule.apply(mancalaGame, segmentSowResult)).findFirst().isPresent();
    }
}
