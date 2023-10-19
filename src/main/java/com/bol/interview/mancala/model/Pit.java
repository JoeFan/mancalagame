package com.bol.interview.mancala.model;

import lombok.Data;

@Data
public class Pit {

    private int stoneNum;

    public Pit(int stoneNum) {
        this.stoneNum = stoneNum;
    }

    public void addStones(int stoneNum) {
        this.stoneNum += stoneNum;
    }

    public void clear() {
        this.stoneNum = 0;
    }

    public void sow() {
        stoneNum +=1;
    }

    public boolean isEmpty() {
        return stoneNum == 0;
    }
}
