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
            playerSegment.addStones2House(playerSegment.clearStonesByPitIdx(lastSowPitIdx));
            int opponentPitIdx = MancalaConstants.LAST_PIT_INDEX - lastSowPitIdx;
            int stoneNum = mancalaGame.getInactiveBoardSegment().clearStonesByPitIdx(opponentPitIdx);
            playerSegment.addStones2House(stoneNum);
            mancalaGame.setActiveBoardSegment(mancalaGame.getInactiveBoardSegment());
            mancalaGame.setInactiveBoardSegment(playerSegment);
            return true;
        }
        return false;

    }
}
