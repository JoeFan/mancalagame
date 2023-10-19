package com.bol.interview.mancala.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMessage<T> {

    public GameMessage(String message, T data){
        this.message = message;
        this.data =data;
    }

    public GameMessage(String message, T data, MessageStatus status){
        this.status = status;
        this.message = message;
        this.data =data;
    }

    private MessageStatus status;

    private String message;

    private T data;


}
