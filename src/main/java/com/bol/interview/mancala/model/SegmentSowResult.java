package com.bol.interview.mancala.model;

import com.bol.interview.mancala.request.SowRequest;
import lombok.Data;

@Data
public class SegmentSowResult {
    private int leftStoneCnt;
    private SowRequest sowRequest;
    private int lastSowPitIndex;
    private String lastPitOwner;

}
