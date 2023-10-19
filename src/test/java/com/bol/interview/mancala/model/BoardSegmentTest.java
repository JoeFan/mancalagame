package com.bol.interview.mancala.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardSegmentTest {

    @Test
    void setPits_whenSowPlayerOwnHouse_expectHouseSowed() {
        String player = "playerA";
        BoardSegment boardSegment = new BoardSegment(player);
        SegmentSowResult segmentSowResult = boardSegment.sow(1, player, 6);
        assertEquals(segmentSowResult.getLeftStoneCnt(), 0);
        assertEquals(boardSegment.getHouseStoneCnt(), 1);
        assertEquals(segmentSowResult.getLastSowPitIndex(), 6);
    }

    @Test
    void setPits_whenSowOpponentHouse_expectHouseNotSow() {
        String player = "playerA";
        BoardSegment boardSegment = new BoardSegment("playerB");
        SegmentSowResult segmentSowResult = boardSegment.sow(1, player, 6);
        assertEquals(segmentSowResult.getLeftStoneCnt(), 1);
        assertEquals(boardSegment.getHouseStoneCnt(), 0);
        assertEquals(segmentSowResult.getLastSowPitIndex(), 5);
    }
}