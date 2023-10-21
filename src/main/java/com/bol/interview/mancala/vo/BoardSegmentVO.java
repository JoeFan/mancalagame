package com.bol.interview.mancala.vo;

import com.bol.interview.mancala.model.BoardSegment;
import lombok.Data;

import java.util.stream.IntStream;

@Data
public class BoardSegmentVO {
    private String segmentId;

    private String player;

    private int[] pits;

    private int house;

    public BoardSegmentVO(BoardSegment booardSegment) {
        this.segmentId = booardSegment.getId();
        this.player = booardSegment.getPlayer();
        this.pits = new int[booardSegment.getPits().size()];
        IntStream.range(0, 6).forEach(idx -> pits[idx] = booardSegment.getPits().get(idx).getStoneNum());
        this.house = booardSegment.getHouseStoneCnt();
    }
}
