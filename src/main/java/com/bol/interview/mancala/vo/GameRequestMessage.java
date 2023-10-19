package com.bol.interview.mancala.vo;


import lombok.Data;

@Data
public class GameRequestMessage {

    private PlayerAction playerAction;
    private String gameId;
    private String userName;
    private int pitIdx;
}
