package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class MoveBothPitsStone2ActivePlayerHouseRule implements GameRule {

    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult segmentSowResult) {
        int lastSowPitIdx = segmentSowResult.getLastSowPitIndex();

        if (segmentSowResult.isLastPitOwnedBySowingPlayer() && segmentSowResult.isLastSowInEmptyPit()) {
            mancalaGame.moveStones2SowingPlayerHouse(lastSowPitIdx);
            mancalaGame.switchTurn();
            return true;
        }
        return false;

    }
}
