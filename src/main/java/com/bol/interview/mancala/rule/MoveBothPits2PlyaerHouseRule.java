package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.BoardSegment;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;
import com.bol.interview.mancala.request.SowRequest;

public class MoveBothPits2PlyaerHouseRule implements GameRule{

    @Override
    public boolean apply(MancalaGame mancalaGame, SegmentSowResult result){
        int lastSowIndex = result.getLastSowPitIndex();
        SowRequest sowRequest = result.getSowRequest();
        String player = sowRequest.getPlayer();
        String lastPitOwner = result.getLastPitOwner();
        BoardSegment playerSegment = mancalaGame.getActiveBoardSegment();

        if(player.equals(lastPitOwner) && playerSegment.getStoneCntByPitIdx(lastSowIndex) == 1){
            playerSegment.addStones2House(playerSegment.clearStonesByPitIdx(lastSowIndex));
            playerSegment.addStones2House(mancalaGame.getInactiveBoardSegment().clearStonesByPitIdx(lastSowIndex));
            mancalaGame.setActiveBoardSegment(mancalaGame.getInactiveBoardSegment());
            mancalaGame.setInactiveBoardSegment(playerSegment);
            return true;
        }
        return false;

    }
}
