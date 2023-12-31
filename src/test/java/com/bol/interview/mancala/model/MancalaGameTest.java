package com.bol.interview.mancala.model;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.request.SowRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MancalaGameTest {

    @Test
    void sow_whenPitIdxLessThan0_expectException() {
        SowRequest sowRequest = new SowRequest("playera", -1);
        MancalaGame mancalaGame = new MancalaGame("playera", "playerb");
        MancalaGameException mancalaGameException = Assertions.assertThrows(MancalaGameException.class,
                ()->mancalaGame.sow(sowRequest));
        assertEquals(mancalaGameException.getMessage(), MancalaConstants.PIT_INDEX_INVALID);

    }
    @Test
    void sow_whenPitIdxLargeThanMaxIdx_expectException() {
        SowRequest sowRequest = new SowRequest("playera", 6);
        MancalaGame mancalaGame = new MancalaGame("playera", "playerb");
        MancalaGameException mancalaGameException = Assertions.assertThrows(MancalaGameException.class,
                ()->mancalaGame.sow(sowRequest));
        assertEquals(mancalaGameException.getMessage(), MancalaConstants.PIT_INDEX_INVALID);
    }


    @Test
    void sow_whenInvalidPlayer_expectException() {

        SowRequest sowRequest = new SowRequest("playera", 1);
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        MancalaGameException mancalaGameException = Assertions.assertThrows(MancalaGameException.class, ()->mancalaGame.sow(sowRequest));
        assertEquals(MancalaConstants.MSG_NOT_PALYER_TURN, mancalaGameException.getMessage());
    }



    @Test
    void sow_whenSowingEmptyPit_expectExceptionWithEmptyMsg() {

        SowRequest sowRequest = new SowRequest("PlayerA", 1);
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        mancalaGame.getActiveBoardSegment().clearStonesByPitIdx(1);
        MancalaGameException mancalaGameException = Assertions.assertThrows(MancalaGameException.class,
                ()->mancalaGame.sow(sowRequest));
        assertEquals(MancalaConstants.MSG_SOWING_EMPTY_PIT, mancalaGameException.getMessage());
    }

    @Test
    void sow_whenPlayerSowOwnEmptyPit_expectMoveBothSidePitStone2PlayerHouse() {

        SowRequest sowRequest = new SowRequest("PlayerA", 0);
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        int lastPitIdx = MancalaConstants.LAST_PIT_INDEX;
        int startPitIdx = 0;

        BoardSegment preInActiveSegment = mancalaGame.getInactiveBoardSegment();
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        int expectHouseStoneCnt = 7;

        preActiveBoardSegment.clearStonesByPitIdx(lastPitIdx);
        preActiveBoardSegment.clearStonesByPitIdx(startPitIdx);

        preActiveBoardSegment.clearStonesByPitIdx(startPitIdx);
        int stoneNum = 5;
        preActiveBoardSegment.addStones2Pit(startPitIdx, stoneNum);
        mancalaGame.sow(sowRequest);

        assertEquals(0, preActiveBoardSegment.getStoneCntByPitIdx(lastPitIdx));
        assertEquals(0, preInActiveSegment.getStoneCntByPitIdx(MancalaConstants.LAST_PIT_INDEX-lastPitIdx));
        assertEquals(expectHouseStoneCnt, preActiveBoardSegment.getHouseStoneCnt());
        assertEquals(preActiveBoardSegment, mancalaGame.getInactiveBoardSegment());
        assertEquals(preInActiveSegment, mancalaGame.getActiveBoardSegment());
    }

    @Test
    void sow_whenSowLastStone2NonEmptyOwnPit_expectSegmentSwitched() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        int lastPitIdx = MancalaConstants.LAST_PIT_INDEX;
        int startPitIdx = 0;

        BoardSegment preInActiveSegment = mancalaGame.getInactiveBoardSegment();
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        int expectLastPitStoneCnt = 7;

        preActiveBoardSegment.clearStonesByPitIdx(startPitIdx);

        preActiveBoardSegment.addStones2Pit(startPitIdx,5);
        SowRequest sowRequest = new SowRequest("PlayerA",0);
        mancalaGame.sow(sowRequest);

        assertEquals(expectLastPitStoneCnt, preActiveBoardSegment.getStoneCntByPitIdx(lastPitIdx));
        assertEquals(preActiveBoardSegment, mancalaGame.getInactiveBoardSegment());
        assertEquals(preInActiveSegment, mancalaGame.getActiveBoardSegment());
    }

    @Test
    void sow_whenSowLastStone2OpponentEmptyPit_expectSegmentSwitched() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        int lastPitIdx = MancalaConstants.LAST_PIT_INDEX;
        int startPitIdx = 0;

        BoardSegment preInActiveSegment = mancalaGame.getInactiveBoardSegment();
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        int expectLastPitStoneCnt = 7;

        preActiveBoardSegment.clearStonesByPitIdx(startPitIdx);
        preInActiveSegment.clearStonesByPitIdx(startPitIdx);
        preActiveBoardSegment.addStones2Pit(startPitIdx,7);
        SowRequest sowRequest = new SowRequest("PlayerA",0);
        mancalaGame.sow(sowRequest);

        assertEquals(expectLastPitStoneCnt, preActiveBoardSegment.getStoneCntByPitIdx(lastPitIdx));
        assertEquals(preActiveBoardSegment, mancalaGame.getInactiveBoardSegment());
        assertEquals(preInActiveSegment, mancalaGame.getActiveBoardSegment());
    }

    @Test
    void sow_whenLastStoneSowInOpponentPit_expectSwitchTurn() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        int startPitIdx = 0;

        BoardSegment preInActiveSegment = mancalaGame.getInactiveBoardSegment();
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();

        preActiveBoardSegment.addStones2Pit(startPitIdx, 2);
        SowRequest sowRequest = new SowRequest("PlayerA", startPitIdx);
        mancalaGame.sow(sowRequest);

        assertEquals(1, preActiveBoardSegment.getHouseStoneCnt());
        assertEquals(preInActiveSegment, mancalaGame.getActiveBoardSegment());
        assertEquals(preActiveBoardSegment, mancalaGame.getInactiveBoardSegment());
    }

    @Test
    void sow_whenSowLastStone2OwnHouse_expectAnotherRound() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        BoardSegment preInActiveSegment = mancalaGame.getInactiveBoardSegment();
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        SowRequest sowRequest = new SowRequest("PlayerA",0);
        mancalaGame.sow(sowRequest);

        assertEquals(1, preActiveBoardSegment.getHouseStoneCnt());
        assertEquals(preActiveBoardSegment, mancalaGame.getActiveBoardSegment());
        assertEquals(preInActiveSegment, mancalaGame.getInactiveBoardSegment());
    }


    @Test
    void sow_whenSowStonePassOppHouse_expectOpponentHouseNotSowed() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        SowRequest sowRequest = new SowRequest("PlayerA",MancalaConstants.LAST_PIT_INDEX);
        preActiveBoardSegment.addStones2Pit(MancalaConstants.LAST_PIT_INDEX, 3);
        mancalaGame.sow(sowRequest);

        assertEquals(1, preActiveBoardSegment.getHouseStoneCnt());
        assertEquals(0, mancalaGame.getActiveBoardSegment().getHouseStoneCnt());
    }

    @Test
    void sow_whenSowSecondPit_expectLastSowedOpponentFirstPit() {

        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        BoardSegment preActiveBoardSegment = mancalaGame.getActiveBoardSegment();
        int pitIdx = 1;
        SowRequest sowRequest = new SowRequest("PlayerA",pitIdx);
        BoardSegment inactiveBoardSegment = mancalaGame.getInactiveBoardSegment();
        int preStoneCnt = inactiveBoardSegment.getStoneCntByPitIdx(0);
        mancalaGame.sow(sowRequest);

        assertEquals(1, preActiveBoardSegment.getHouseStoneCnt());
        assertEquals(0, mancalaGame.getActiveBoardSegment().getHouseStoneCnt());
        assertEquals(preStoneCnt+1, inactiveBoardSegment.getStoneCntByPitIdx(0));
    }


    @Test
    void moveStones2SowingPlayerHouse_whenmoving_activePlayerHouseStoneCntIncrease(){
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        mancalaGame.moveStones2SowingPlayerHouse(5);
        assertEquals(12, mancalaGame.getActiveBoardSegment().getHouseStoneCnt());
    }

    @Test
    void switchTurn_whenswitched_expectBoardswitched(){
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        BoardSegment activeSegment = mancalaGame.getActiveBoardSegment();
        BoardSegment inactiveSegment = mancalaGame.getInactiveBoardSegment();
        mancalaGame.switchTurn();
        assertEquals(activeSegment, mancalaGame.getInactiveBoardSegment());
        assertEquals(inactiveSegment, mancalaGame.getActiveBoardSegment());

    }

    @Test
    void getSuccessInfo_whenGameIsNotOver_expectEmptyResult(){
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        assertEquals(MancalaConstants.BLANK_INFO, mancalaGame.getSuccessInfo());
    }

    @Test
    void getSuccessInfo_whenEqualResult_expectGameResultEqual(){
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        mancalaGame.getInactiveBoardSegment().moveAllPits2House();
        mancalaGame.getActiveBoardSegment().moveAllPits2House();
        assertEquals(MancalaConstants.GAME_RESULT_EQUAL, mancalaGame.getSuccessInfo());
    }

    @Test
    void getSuccessInfo_whenOnePlayerWin_expectWinnerPlayerInfo(){
        MancalaGame mancalaGame = new MancalaGame("PlayerA", "PlayerB");
        mancalaGame.getInactiveBoardSegment().moveAllPits2House();
        mancalaGame.getActiveBoardSegment().moveAllPits2House();
        mancalaGame.getActiveBoardSegment().addStones2House(2);
        assertEquals("PlayerA", mancalaGame.getSuccessInfo());
    }


}