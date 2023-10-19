package com.bol.interview.mancala.vo;

import com.bol.interview.mancala.model.MancalaGame;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MancalaGameVO {

    private String gameId;

    private BoardSegmentVO activeBoardSegmentVO;
    private BoardSegmentVO inactiveBoardSegmentVO;

    private String gameInfo;

    private boolean isGameOver;


    public MancalaGameVO(MancalaGame game) {
        this.gameId = game.getGameId();
        activeBoardSegmentVO = new BoardSegmentVO(game.getActiveBoardSegment());
        inactiveBoardSegmentVO = new BoardSegmentVO(game.getInactiveBoardSegment());
        this.isGameOver = game.isGameOver();
        this.gameInfo = game.getSuccessInfo();

    }


}
