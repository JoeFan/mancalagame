package com.bol.interview.mancala.util;

import com.bol.interview.mancala.service.MancalaEndpoint;
import com.bol.interview.mancala.vo.GameMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public class MessageSender{

    private static ObjectMapper mapper = new ObjectMapper();


    public static void sendMessage2Endpoints(GameMessage gameMessage, Collection<MancalaEndpoint> mancalaEndpoints) {
        mancalaEndpoints.forEach(mancalaEndpoint -> {
            sendMessage2Endpoint(gameMessage, mancalaEndpoint);
        });

    }

    public static void sendMessage2Endpoint(GameMessage gameMessage, MancalaEndpoint mancalaEndpoint) {
        try {
            String msgStr = mapper.writeValueAsString(gameMessage);
            mancalaEndpoint.getSession().getBasicRemote().sendText(msgStr);
        } catch (IOException e) {
            mancalaEndpoint.onClose();
        }
    }

}