package com.bol.interview.mancala.model;

import com.bol.interview.mancala.request.SowRequest;
import lombok.Data;

import java.util.List;

@Data
public class BoardSegment {

    private String id;
    private String player;
    private Pit house;
    private List<Pit> pits;

    public int clearStonesByPitIdx(int pitIdx) {
        int stoneCnt = pits.get(pitIdx).getStoneCnt();
        pits.get(pitIdx).clear();
        return stoneCnt;
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

    public SegmentSowResult sow(SowRequest sowRequest, int stoneCnt) {
        int pitIdx = sowRequest.getPitIdx();
        while (pitIdx < pits.size() && stoneCnt > 0) {
            pits.get(pitIdx).sow();
            stoneCnt--;
            pitIdx++;
        }
        if(stoneCnt > 0 && this.player.equals(sowRequest.getPlayer())){
            house.sow();
            stoneCnt--;
            pitIdx++;
        }
        return getSowResult(stoneCnt, pitIdx-1, sowRequest.getPlayer());
    }

    private SegmentSowResult getSowResult(int stoneCnt, int pitIdx, String sowedPlayer) {
        SegmentSowResult segmentSowResult = new SegmentSowResult();
        segmentSowResult.setLeftStoneCnt(stoneCnt);
        segmentSowResult.setLastSowPitIndex(pitIdx);
        segmentSowResult.setSowRequestPlayer(sowedPlayer);
        segmentSowResult.setLastPitOwner(this.player);
        return segmentSowResult;
    }

    public void addStones2House(int stoneNum) {
        house.addStones(stoneNum);
    }
}
