package com.bol.interview.mancala.model;

import lombok.Data;

import java.util.List;

@Data
public class BoardSegment {

    private String id;
    private String player;
    private Pit house;
    private List<Pit> pits;
}
