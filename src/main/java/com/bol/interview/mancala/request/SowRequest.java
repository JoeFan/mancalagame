package com.bol.interview.mancala.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SowRequest {

    private String player;
    private int pitIdx;

}
