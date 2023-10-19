package com.bol.interview.mancala.model;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.request.SowRequest;
import com.bol.interview.mancala.rule.MancalaGameRules;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document("mancalagame")
public class MancalaGame {

    private BoardSegment activeBoardSegment;

    private BoardSegment inactiveBoardSegment;

    @Id
    private String gameId;

    public MancalaGame(String activePlayer, String player) {
        this.gameId = UUID.randomUUID().toString();
        activeBoardSegment = new BoardSegment(activePlayer);
        inactiveBoardSegment = new BoardSegment(player);
    }

    public void sow(SowRequest sowRequest){
        checkSowRequest(sowRequest);
        int stoneCnt = activeBoardSegment.clearStonesByPitIdx(sowRequest.getPitIdx());
        SegmentSowResult segmentSowResult = sow(sowRequest, stoneCnt);
        MancalaGameRules.applyRules(this, segmentSowResult);

    }

    private SegmentSowResult sow(SowRequest sowRequest, int stoneCnt) {
        BoardSegment currentBoardSegment = activeBoardSegment;
        BoardSegment nextBoardSegment = inactiveBoardSegment;
        SegmentSowResult segmentSowResult = null;
        while(stoneCnt != 0){
            segmentSowResult = currentBoardSegment.sow(sowRequest, stoneCnt);
            stoneCnt = segmentSowResult.getLeftStoneCnt();
            BoardSegment temp = currentBoardSegment;
            currentBoardSegment = nextBoardSegment;
            nextBoardSegment = temp;
            sowRequest.setPitIdx(0);
        }

        return segmentSowResult;
    }

    private void checkSowRequest(SowRequest sowRequest) {
        if(sowRequest.getPitIdx() < 0 || sowRequest.getPitIdx() > 5){
            throw new MancalaGameException(MancalaConstants.PIT_INDEX_INVALID);
        }
    }


    public boolean isGameOver() {
        return false;
    }

    public String getSuccessInfo() {
        if (isGameOver()) {
            int houseStoneCnt = activeBoardSegment.getHouseStoneCnt();
            int inactiveBoardSegmentHouseStoneCnt = inactiveBoardSegment.getHouseStoneCnt();
            if (houseStoneCnt == inactiveBoardSegmentHouseStoneCnt) {
                return MancalaConstants.GAME_RESULT_EQUAL;
            }
            return houseStoneCnt > inactiveBoardSegmentHouseStoneCnt ?
                    activeBoardSegment.getPlayer() : inactiveBoardSegment.getPlayer();
        }
        return "";

    }
}
