package com.bol.interview.mancala.model;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.request.SowRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}