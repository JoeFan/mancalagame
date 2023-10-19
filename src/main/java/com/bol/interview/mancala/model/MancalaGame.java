package com.bol.interview.mancala.model;

import com.bol.interview.mancala.request.SowRequest;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("mancalagame")
public class MancalaGame {

    private BoardSegment activeBoardSegment;

    private BoardSegment inactiveBoardSegment;

    private String gameId;

    public void sow(SowRequest sowRequest){

    }
}
