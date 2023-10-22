package com.bol.interview.mancala.util;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.service.MancalaEndpoint;
import com.bol.interview.mancala.vo.GameMessage;
import com.bol.interview.mancala.vo.GameRequestMessage;
import com.bol.interview.mancala.vo.MancalaGameVO;
import com.bol.interview.mancala.vo.MessageStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

@Slf4j
public class MessageSender {

    private static final ObjectMapper mapper = new ObjectMapper();


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


    public static void sendRoomIsFullMessage(MancalaEndpoint mancalaEndpoint) {
        GameMessage value = new GameMessage(MancalaConstants.MSG_ROOM_IS_FULL, MessageStatus.OPERATION_ERR);
        MessageSender.sendMessage2Endpoint(value, mancalaEndpoint);
    }

    public static void sendPlayerNameExistMsg(String username, MancalaEndpoint mancalaEndpoint) {
        String msg = MessageFormat.format(MancalaConstants.MSG_USER_NAME_EXIST, username);
        GameMessage value = new GameMessage(msg, MessageStatus.OPERATION_ERR);
        MessageSender.sendMessage2Endpoint(value, mancalaEndpoint);
    }

    public static void sendPlayerIsReadyMessage(String username, Collection<MancalaEndpoint> mancalaEndpoints) {
        String playerIsReady = MessageFormat.format(MancalaConstants.MSG_PLAYER_IS_READY, username);
        GameMessage gameMessage = new GameMessage(playerIsReady, MessageStatus.READY);
        MessageSender.sendMessage2Endpoints(gameMessage, mancalaEndpoints);
    }

    public static void sendGameStartMessage(MancalaGame mancalaGame, Collection<MancalaEndpoint> mancalaEndpoints) {
        String message = MessageFormat.format(MancalaConstants.MSG_GAME_START, mancalaGame.getActiveBoardSegment().getPlayer());
        MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
        GameMessage<MancalaGameVO> gameMessage = new GameMessage(message, mancalaGameVO, MessageStatus.START);
        MessageSender.sendMessage2Endpoints(gameMessage, mancalaEndpoints);
    }

    public static void sendSowedResultMessage(GameRequestMessage gameRequestMessage, MancalaGameVO mancalaGameVO, Collection<MancalaEndpoint> mancalaEndpoints) {
        if (mancalaGameVO.isGameOver()) {
            sendGameOverMessage(mancalaGameVO, mancalaEndpoints);
        } else {
            sendSowingResultMessage(gameRequestMessage, mancalaGameVO, mancalaEndpoints);
        }
    }

    private static void sendSowingResultMessage(GameRequestMessage gameRequestMessage, MancalaGameVO mancalaGameVO, Collection<MancalaEndpoint> mancalaEndpoints) {
        String msg = MessageFormat.format(MancalaConstants.MSG_PLAYER_SOW_WITH_PIT_INDEX, gameRequestMessage.getUserName(), gameRequestMessage.getPitIdx());
        GameMessage sowGameMessage = new GameMessage<>(msg, mancalaGameVO, MessageStatus.SOW);

        MessageSender.sendMessage2Endpoints(sowGameMessage, mancalaEndpoints);
    }

    private static void sendGameOverMessage(MancalaGameVO mancalaGameVO, Collection<MancalaEndpoint> mancalaEndpoints) {
        GameMessage sowGameMessage = new GameMessage<>(mancalaGameVO.getGameInfo(), mancalaGameVO, MessageStatus.END);
        MessageSender.sendMessage2Endpoints(sowGameMessage, mancalaEndpoints);
    }

    public static void sendOperationErrorMessage(String message, MancalaEndpoint mancalaEndpoint) {
        GameMessage gameMessage = new GameMessage(message, MessageStatus.OPERATION_ERR);
        MessageSender.sendMessage2Endpoint(gameMessage, mancalaEndpoint);
    }

}