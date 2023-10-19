package com.bol.interview.mancala.model;

import lombok.Data;

@Data
public class SegmentSowResult {
    private int leftStoneCnt;
    private String sowRequestPlayer;
    private int lastSowPitIndex;
    private String lastPitOwner;

}
