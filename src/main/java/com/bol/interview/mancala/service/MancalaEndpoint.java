package com.bol.interview.mancala.service;

import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.repository.MancalaGameRepository;
import com.bol.interview.mancala.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@Component
@ServerEndpoint(value = "/mancala/{username}")
@Slf4j
public class MancalaEndpoint {

    private Session session;

    private static ObjectMapper mapper = new ObjectMapper();

    private String player;

    private static MancalaGameRepository gameRepository;

    @Autowired
    public void setGameRepository(MancalaGameRepository mancalaGameRepository) {
        this.gameRepository = mancalaGameRepository;
    }

    private static final Map<String, MancalaEndpoint> WEB_SOCKET_CHESS_GAMER = new HashMap();
    private static final int LENGTH = 2;


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        if (WEB_SOCKET_CHESS_GAMER.size() == LENGTH) {
            sendMessage(this, "The room is Full!");
            this.onClose();
            return;
        }
        this.player = username;
        this.session = session;
        if (WEB_SOCKET_CHESS_GAMER.containsKey(username)) {
            sendMessage(this, "user name " + username + " is already exist! please rename a new one!");
        }
        WEB_SOCKET_CHESS_GAMER.put(player, this);
        sendGameMessageOnCoonectionOpen(username);
    }

    private static void sendGameMessageOnCoonectionOpen(String username) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.setStatus(MessageStatus.READY);
        gameMessage.setMessage(username);
        sendChatMessage(gameMessage);
    }


    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("message==>{}", message);
        GameRequestMessage rep = mapper.readValue(message, GameRequestMessage.class);
        log.info("resultObject===>{}", rep.toString());
        //  int status = rep.getStatus();
        PlayerAction playerAction = rep.getPlayerAction();

        if (playerAction == PlayerAction.START) {
            GameMessage gameMessage = new GameMessage();
            gameMessage.setStatus(MessageStatus.READY);
            gameMessage.setMessage(this.player + " is ready......");

            if (isPrepareAll()) {
                synchronized (WEB_SOCKET_CHESS_GAMER) {
                    Object[] players = WEB_SOCKET_CHESS_GAMER.keySet().toArray();
                    MancalaGame mancalaGame = new MancalaGame(players[0].toString(), players[1].toString());
                    gameRepository.save(mancalaGame);
                    MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                    gameMessage.setData(mancalaGameVO);

                    gameMessage.setStatus(MessageStatus.START);
                    gameMessage.setMessage("Game started! It's " + mancalaGame.getActiveBoardSegment().getPlayer() + "'s turn!");
                }
            }
            sendChatMessage(gameMessage);
        } else if (playerAction == PlayerAction.SOW) {
            try {
                String gameId = rep.getGameId();

                Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
                if (optionalMancalaGame.isPresent()) {
                    MancalaGame mancalaGame = optionalMancalaGame.get();
                    int pitIdx = rep.getPitIdx();
                    mancalaGame.sow(player, pitIdx);

                    MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                    maintainGamePersistence(mancalaGame, mancalaGameVO);
                    sendChatMessage(getSowGameMessage(mancalaGame, pitIdx, mancalaGameVO));
                }

            } catch (MancalaGameException e) {
                sendOperationInvalidMessage(e);
            }


        }
    }

    private GameMessage getSowGameMessage(MancalaGame mancalaGame, int pitIdx, MancalaGameVO mancalaGameVO) {
        GameMessage gameMessage = new GameMessage();
        gameMessage.setData(mancalaGameVO);
        if (mancalaGame.isGameOver()) {
            gameMessage.setMessage(mancalaGameVO.getGameInfo());
            gameMessage.setStatus(MessageStatus.END);
        } else {
            gameMessage.setMessage("Player " + player + " sow pit Idx " + pitIdx);
            gameMessage.setStatus(MessageStatus.SOW);
        }
        return gameMessage;
    }

    private static void maintainGamePersistence(MancalaGame mancalaGame, MancalaGameVO mancalaGameVO) {
        if (mancalaGameVO.isGameOver()) {
            gameRepository.deleteById(mancalaGameVO.getGameId());
        } else {
            gameRepository.save(mancalaGame);
        }
    }

    private void sendOperationInvalidMessage(MancalaGameException e) throws JsonProcessingException {
        GameMessage gameMessage = new GameMessage();
        gameMessage.setStatus(MessageStatus.OPERATION_ERR);
        gameMessage.setMessage(e.getMessage());
        this.sendMessage(mapper.writeValueAsString(gameMessage));
    }


    public void sendMessage(MancalaEndpoint chessServer, String msg) {
        try {
            chessServer.session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            chessServer.onClose();
        }
    }

    public void sendMessage(String msg) {
        sendMessage(this, msg);
    }


    public static void sendChatMessage(GameMessage gameMessage) {
        Set<String> keySet = WEB_SOCKET_CHESS_GAMER.keySet();
        MancalaEndpoint chessServer = null;
        for (String username : keySet) {
            chessServer = WEB_SOCKET_CHESS_GAMER.get(username);
            String msg = null;
            try {
                msg = mapper.writeValueAsString(gameMessage);
            } catch (JsonProcessingException e) {
                msg = e.getMessage();
                throw new MancalaGameException(e.getMessage(), e);
            } finally {
                chessServer.sendMessage(msg);
            }

        }
    }

    @OnClose
    public void onClose() {
        WEB_SOCKET_CHESS_GAMER.remove(this.player);
        if (this.session.isOpen()) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("session close error", e);
            }
        }

    }


    protected Integer getColor() {
        int size = WEB_SOCKET_CHESS_GAMER.size();
        return size == 1 ? 1 : -1;
    }


    public static boolean isPrepareAll() {
        Collection<MancalaEndpoint> values = WEB_SOCKET_CHESS_GAMER.values();
        if (values.size() != 2)
            return false;
        return true;
    }


}
