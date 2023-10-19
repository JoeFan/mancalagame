package com.bol.interview.mancala.rule;

import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.model.SegmentSowResult;

public interface GameRule {

    boolean apply(MancalaGame mancalaGame, SegmentSowResult result);
}
