package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class AnotherRoundRule implements GameRule {

    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result) {
        int lastSowPitIdx = result.getLastSowPitIndex();
        return lastSowPitIdx == MancalaConstants.HOUSE_INDEX;
    }

}
