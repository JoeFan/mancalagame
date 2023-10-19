package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.model.BoardSegment;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public class MoveBothPits2PlayerHouseRule implements GameRule{

    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result){
        int lastSowIndex = result.getLastSowPitIndex();
        String sowRequestPlayer = result.getSowRequestPlayer();
        String lastPitOwner = result.getLastPitOwner();
        BoardSegment playerSegment = mancalaGame.getActiveBoardSegment();

        if(sowRequestPlayer.equals(lastPitOwner) && playerSegment.getStoneCntByPitIdx(lastSowIndex) == 1){
            playerSegment.addStones2House(playerSegment.clearStonesByPitIdx(lastSowIndex));
            int stoneNum = mancalaGame.getInactiveBoardSegment().clearStonesByPitIdx(MancalaConstants.PITS_NUM - lastSowIndex);
            playerSegment.addStones2House(stoneNum);
            mancalaGame.setActiveBoardSegment(mancalaGame.getInactiveBoardSegment());
            mancalaGame.setInactiveBoardSegment(playerSegment);
            return true;
        }
        return false;

    }
}
