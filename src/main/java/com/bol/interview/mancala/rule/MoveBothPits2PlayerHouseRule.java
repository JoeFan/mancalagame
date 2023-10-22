package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.model.BoardSegment;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class MoveBothPits2PlayerHouseRule implements GameRule {

    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult segmentSowResult) {
        int lastSowPitIdx = segmentSowResult.getLastSowPitIndex();
        BoardSegment playerSegment = mancalaGame.getActiveBoardSegment();

        if (segmentSowResult.isLastOwnPlayerOwnPit() && segmentSowResult.isLastSowInEmptyPit()) {
            playerSegment.movePits2PlayerHouse(playerSegment, lastSowPitIdx);
            int opponentPitIdx = MancalaConstants.LAST_PIT_INDEX - lastSowPitIdx;
            mancalaGame.getInactiveBoardSegment().movePits2PlayerHouse(playerSegment, opponentPitIdx);
            mancalaGame.switchTurn();
            return true;
        }
        return false;

    }
}
