package com.bol.interview.mancala.model;

import lombok.Data;

import java.util.List;

@Data
public class BoardSegment {

    private String id;
    private String player;
    private Pit house;
    private List<Pit> pits;

    public void clearStonesByPitIdx(int pitIdx) {
        pits.get(pitIdx).clear();
    }

    public void addStones2Pit(int pitIdx, int stoneNum) {
        pits.get(pitIdx).addStones(stoneNum);
    }

    public int getStoneCntByPitIdx(int pitIdx) {
        return pits.get(pitIdx).getStoneCnt();
    }

    public int getHouseStoneCnt() {
        return house.getStoneCnt();
    }
}
