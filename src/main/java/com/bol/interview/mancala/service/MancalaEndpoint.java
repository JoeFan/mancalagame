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
    public void setGameRepository(MancalaGameRepository mancalaGameRepository) {
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
        sendMessage2Endpoint(buildGameMessageWithStatus(username, MessageStatus.READY), WEB_SOCKET_CHESS_GAMER.get(username));
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

        if (isPrepareAll()) {
            synchronized (WEB_SOCKET_CHESS_GAMER) {
                Object[] players = WEB_SOCKET_CHESS_GAMER.keySet().toArray();
                MancalaGame mancalaGame = new MancalaGame(players[0].toString(), players[1].toString());
                this.gameId = mancalaGame.getGameId();
                persistGamePlayers(mancalaGame);
                GameMessage<MancalaGameVO> gameMessage = buildGameStartMessage(mancalaGame);
                sendMessage2Endpoint(gameMessage, GAME_PLAYERS_ENDPOINT.get(gameId));
            }
        }else{
            sendMessage2Endpoint(buildGameMessageWithStatus(this.player + " is ready......", MessageStatus.READY), this);
        }


    }

    private void sowPits(GameRequestMessage rep) throws JsonProcessingException {
        try {
            String gameId = rep.getGameId();
            if(this.gameId == null){
                this.gameId = gameId;
            }
            Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
            if (optionalMancalaGame.isPresent()) {
                MancalaGame mancalaGame = optionalMancalaGame.get();
                mancalaGame.sow(new SowRequest(player, rep.getPitIdx()));
                MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                maintainGamePersistence(mancalaGame, mancalaGameVO);
                sendMessage2Endpoint(getSowGameMessage(mancalaGame, rep.getPitIdx(), mancalaGameVO), GAME_PLAYERS_ENDPOINT.get(gameId));
            }

        } catch (MancalaGameException e) {
            sendOperationInvalidMessage(e);
        }
    }

    private GameMessage<String> buildGameMessageWithStatus(String message, MessageStatus messageStatus) {
        GameMessage<String> gameMessage = new GameMessage<>(message, messageStatus);
        return gameMessage;
    }

    private GameMessage<MancalaGameVO> buildGameStartMessage(MancalaGame mancalaGame) {
        String message = "Game started! It's " + mancalaGame.getActiveBoardSegment().getPlayer() + "'s turn!";
        MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
        GameMessage<MancalaGameVO> gameMessage = new GameMessage(message, mancalaGameVO, MessageStatus.START);
        return gameMessage;
    }


    private void persistGamePlayers(MancalaGame mancalaGame) {
        gameRepository.save(mancalaGame);
        List<MancalaEndpoint> gameEndPoints = new ArrayList<>();
        gameEndPoints.addAll(WEB_SOCKET_CHESS_GAMER.values());
        GAME_PLAYERS_ENDPOINT.put(mancalaGame.getGameId(), gameEndPoints);
        WEB_SOCKET_CHESS_GAMER.clear();
    }

    private GameMessage getSowGameMessage(MancalaGame mancalaGame, int pitIdx, MancalaGameVO mancalaGameVO) {
        GameMessage gameMessage = null;
        if (mancalaGame.isGameOver()) {
            gameMessage = buildGameOverMessage(mancalaGameVO, mancalaGameVO.getGameInfo(), MessageStatus.END);
        } else {
            gameMessage = buildGameOverMessage(mancalaGameVO, "Player " + player + " sow pit Idx " + pitIdx, MessageStatus.SOW);
        }
        return gameMessage;
    }

    private GameMessage<MancalaGameVO> buildGameOverMessage(MancalaGameVO mancalaGameVO, String gameInfo, MessageStatus end) {
        GameMessage<MancalaGameVO> gameMessage = new GameMessage<>(gameInfo, mancalaGameVO, end);
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
        GameMessage<String> gameMessage = new GameMessage(e.getMessage(), MessageStatus.OPERATION_ERR);
        this.sendMessage(mapper.writeValueAsString(gameMessage));
    }


    public void sendMessage(MancalaEndpoint chessServer, String msg) {
        try {
            chessServer.getSession().getBasicRemote().sendText(msg);
        } catch (IOException e) {
            chessServer.onClose();
        }
    }

    public void sendMessage(String msg) {
        sendMessage(this, msg);
    }


    public void sendMessage2Endpoint(GameMessage gameMessage, List<MancalaEndpoint> mancalaEndpoints) {
        mancalaEndpoints.forEach(mancalaEndpoint -> {
            sendMessage2Endpoint(gameMessage, mancalaEndpoint);
        });

    }

    public void sendMessage2Endpoint(GameMessage gameMessage, MancalaEndpoint mancalaEndpoint) {
        String msg = null;
        try {
            msg = mapper.writeValueAsString(gameMessage);
        } catch (JsonProcessingException e) {
            msg = e.getMessage();
            log.error("Json format error ", e);
            throw new MancalaGameException(msg, e);
        } finally {
            mancalaEndpoint.sendMessage(msg);
        }

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

    public Session getSession(){
        return this.session;
    }


}
