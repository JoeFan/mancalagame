package com.bol.interview.mancala.model;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.request.SowRequest;
import com.bol.interview.mancala.rule.MancalaGameRules;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Optional;

@Getter
@Setter
@Document("mancalagame")
public class MancalaGame {

    private BoardSegment activeBoardSegment;

    private BoardSegment inactiveBoardSegment;

    private String gameId;

    public MancalaGame(String playera, String playerb) {

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


}
