package com.bol.interview.mancala.model;

import com.bol.interview.mancala.constants.MancalaConstants;
import lombok.Data;

import java.util.*;
import java.util.stream.IntStream;

@Data
public class BoardSegment {


    private String id;
    private String player;
    private Pit house;
    private List<Pit> pits;

    public BoardSegment(String player) {
        this.player = player;
        this.id = UUID.randomUUID().toString();
        initPits();

    }

    private void initPits() {
        List<Pit> tempPitList = new ArrayList<>();
        IntStream.range(0, MancalaConstants.PITS_NUM).forEach(idx ->
                tempPitList.add(new Pit(MancalaConstants.INIT_STONE_NUM)));
        house = new Pit(MancalaConstants.INIT_HOUSE_STONE_CNT);
        pits = Collections.unmodifiableList(tempPitList);
    }

    public int clearStonesByPitIdx(int pitIdx) {
        int stoneCnt = pits.get(pitIdx).getStoneNum();
        pits.get(pitIdx).clear();
        return stoneCnt;
    }

    public boolean allPitsEmpty() {
        Optional<Pit> nonEmptyPit = pits.stream().filter(pit->!pit.isEmpty()).findFirst();
        return !nonEmptyPit.isPresent();
    }

    public void addStones2Pit(int pitIdx, int stoneNum) {
        pits.get(pitIdx).addStones(stoneNum);
    }

    public int getStoneCntByPitIdx(int pitIdx) {
        return pits.get(pitIdx).getStoneNum();
    }

    public int getHouseStoneCnt() {
        return house.getStoneNum();
    }

    public SegmentSowResult sow(int pitIdx, String sowingPlayer, int stoneCnt) {
        while (pitIdx < pits.size() && stoneCnt > 0) {
            pits.get(pitIdx).sow();
            stoneCnt--;
            pitIdx++;
        }
        if(stoneCnt > 0 && this.player.equals(sowingPlayer)){
            house.sow();
            stoneCnt--;
            pitIdx++;
        }
        return getSowResult(stoneCnt, pitIdx-1, sowingPlayer);
    }

    private SegmentSowResult getSowResult(int stoneCnt, int pitIdx, String sowedPlayer) {
        SegmentSowResult segmentSowResult = new SegmentSowResult();
        segmentSowResult.setLeftStoneCnt(stoneCnt);
        segmentSowResult.setLastSowPitIndex(pitIdx);
        segmentSowResult.setSowRequestPlayer(sowedPlayer);
        segmentSowResult.setLastPitOwner(this.player);
        segmentSowResult.setLastSowInEmptyPit(this.getStoneCntByPitIdx(pitIdx) == 1);
        return segmentSowResult;
    }

    public void addStones2House(int stoneNum) {
        house.addStones(stoneNum);
    }

    public void moveAllPits2House() {
        pits.forEach(pit -> {
            house.addStones(pit.removalAllStones());
        });
    }
}
