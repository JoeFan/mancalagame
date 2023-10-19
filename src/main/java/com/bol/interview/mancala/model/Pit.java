package com.bol.interview.mancala.model;

import lombok.Data;

@Data
public class Pit {
    private int idx;
    private int stoneCnt;

    public void addStones(int stoneNum) {
        stoneCnt += stoneNum;
    }

    public void clear() {
        this.stoneCnt = 0;
    }

    public void sow() {
        stoneCnt+=1;
    }
}
