package com.bol.interview.mancala.service;

import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.repository.MancalaGameRepository;
import com.bol.interview.mancala.request.SowRequest;
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

    private String gameId;

    private static MancalaGameRepository gameRepository;

    @Autowired
    public static void setGameRepository(MancalaGameRepository mancalaGameRepository) {
        gameRepository = mancalaGameRepository;
    }

    private static final Map<String, MancalaEndpoint> WEB_SOCKET_CHESS_GAMER = new HashMap<>();
    private static final Map<String, List<MancalaEndpoint>> GAME_PLAYERS_ENDPOINT = new HashMap<>();
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
        sendChatMessage(buildGameMessageWithStatus(username, MessageStatus.READY), GAME_PLAYERS_ENDPOINT.get(gameId));
    }


    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("message==>{}", message);
        GameRequestMessage gameRequestMessage = mapper.readValue(message, GameRequestMessage.class);
        log.info("resultObject===>{}", gameRequestMessage.toString());

        PlayerAction playerAction = gameRequestMessage.getPlayerAction();
        if (playerAction == PlayerAction.START) {
            initializeGame();
        } else if (playerAction == PlayerAction.SOW) {
            sowPits(gameRequestMessage);
        }
    }

    private void initializeGame() {
        GameMessage gameMessage = buildGameMessageWithStatus(this.player + " is ready......", MessageStatus.READY);

        synchronized (WEB_SOCKET_CHESS_GAMER) {
            if (isPrepareAll()) {
                Object[] players = WEB_SOCKET_CHESS_GAMER.keySet().toArray();
                MancalaGame mancalaGame = new MancalaGame(players[0].toString(), players[1].toString());

                persistGamePlayers(mancalaGame);
                gameMessage = buildGameStartMessage(mancalaGame);
            }
        }
        sendChatMessage(gameMessage, GAME_PLAYERS_ENDPOINT.get(gameId));
    }

    private void sowPits(GameRequestMessage rep) throws JsonProcessingException {
        try {
            Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
            if (optionalMancalaGame.isPresent()) {
                MancalaGame mancalaGame = optionalMancalaGame.get();
                mancalaGame.sow(new SowRequest(player, rep.getPitIdx()));
                MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                maintainGamePersistence(mancalaGame, mancalaGameVO);
                sendChatMessage(getSowGameMessage(mancalaGame, rep.getPitIdx(), mancalaGameVO), GAME_PLAYERS_ENDPOINT.get(gameId));
            }

        } catch (MancalaGameException e) {
            sendOperationInvalidMessage(e);
        }
    }

    private GameMessage<String> buildGameMessageWithStatus(String message, MessageStatus messageStatus) {
        GameMessage<String> gameMessage = new GameMessage<>();
        gameMessage.setStatus(messageStatus);
        gameMessage.setMessage(message);
        return gameMessage;
    }

    private GameMessage<MancalaGameVO> buildGameStartMessage(MancalaGame mancalaGame) {
        GameMessage<MancalaGameVO> gameMessage = new GameMessage<>();
        MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
        gameMessage.setData(mancalaGameVO);
        gameMessage.setStatus(MessageStatus.START);
        gameMessage.setMessage("Game started! It's " + mancalaGame.getActiveBoardSegment().getPlayer() + "'s turn!");
        return gameMessage;
    }


    private void persistGamePlayers(MancalaGame mancalaGame) {
        gameRepository.save(mancalaGame);
        List<MancalaEndpoint> gameEndPoints = new ArrayList<>();
        gameEndPoints.addAll(WEB_SOCKET_CHESS_GAMER.values());
        GAME_PLAYERS_ENDPOINT.put(mancalaGame.getGameId(), gameEndPoints);
        WEB_SOCKET_CHESS_GAMER.clear();
    }

    private GameMessage<String> getSowGameMessage(MancalaGame mancalaGame, int pitIdx, MancalaGameVO mancalaGameVO) {
        GameMessage<String> gameMessage = null;
        if (mancalaGame.isGameOver()) {
            buildGameOverMessage(mancalaGameVO, mancalaGameVO.getGameInfo(), MessageStatus.END);
        } else {
            buildGameOverMessage(mancalaGameVO, "Player " + player + " sow pit Idx " + pitIdx, MessageStatus.SOW);
        }
        return gameMessage;
    }

    private GameMessage<MancalaGameVO> buildGameOverMessage(MancalaGameVO mancalaGameVO, String gameInfo, MessageStatus end) {
        GameMessage<MancalaGameVO> gameMessage = new GameMessage<>();
        gameMessage.setData(mancalaGameVO);
        gameMessage.setMessage(gameInfo);
        gameMessage.setStatus(end);
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
        GameMessage<String> gameMessage = buildGameMessageWithStatus(e.getMessage(), MessageStatus.OPERATION_ERR);
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


    public void sendChatMessage(GameMessage<String> gameMessage, List<MancalaEndpoint> mancalaEndpoints) {
        mancalaEndpoints.forEach(mancalaEndpoint -> {
            String msg = null;
            try {
                msg = mapper.writeValueAsString(gameMessage);
            } catch (JsonProcessingException e) {
                msg = e.getMessage();
                throw new MancalaGameException(e.getMessage(), e);
            } finally {
                mancalaEndpoint.sendMessage(msg);
            }
        });

    }

    @OnClose
    public void onClose() {
        GAME_PLAYERS_ENDPOINT.remove(this.gameId);
        if (this.session.isOpen()) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("session close error", e);
            }
        }

    }


    public static boolean isPrepareAll() {
        Collection<MancalaEndpoint> values = WEB_SOCKET_CHESS_GAMER.values();
        return values.size() >= 2;
    }


}
