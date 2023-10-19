package com.bol.interview.mancala.model;

import com.bol.interview.mancala.request.SowRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("mancalagame")
public class MancalaGame {

    private BoardSegment activeBoardSegment;

    private BoardSegment inactiveBoardSegment;

    private String gameId;

    public MancalaGame(String playera, String playerb) {

    }

    public void sow(SowRequest sowRequest){

    }


}
