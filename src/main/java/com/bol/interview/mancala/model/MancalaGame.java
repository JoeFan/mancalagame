package com.bol.interview.mancala.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class MancalaGame {

    private BoardSegment activeBoardSegment;

    private BoardSegment inactiveBoardSegment;

    private String gameId;
}
