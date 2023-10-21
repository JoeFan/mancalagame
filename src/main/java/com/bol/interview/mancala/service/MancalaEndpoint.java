package com.bol.interview.mancala.service;

import com.bol.interview.mancala.constants.MancalaConstants;
import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.repository.MancalaGameRepository;
import com.bol.interview.mancala.request.SowRequest;
import com.bol.interview.mancala.util.MessageSender;
import com.bol.interview.mancala.vo.*;
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
import java.text.MessageFormat;
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

    private static final Map<String, MancalaEndpoint> WEB_SOCKET_MANCALA_GAMER = new HashMap<>();
    private static final Map<String, List<MancalaEndpoint>> GAME_PLAYERS_ENDPOINT = new HashMap<>();
    private static final int LENGTH = 2;

    @Autowired
    public void setGameRepository(MancalaGameRepository mancalaGameRepository) {
        gameRepository = mancalaGameRepository;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        if (WEB_SOCKET_MANCALA_GAMER.size() == LENGTH) {
            GameMessage value = new GameMessage(MancalaConstants.MSG_ROOM_IS_FULL, MessageStatus.OPERATION_ERR);
            MessageSender.sendMessage2Endpoint(value,this);
            return;
        }

        if (WEB_SOCKET_MANCALA_GAMER.containsKey(username)) {
            String msg = MessageFormat.format(MancalaConstants.MSG_USER_NAME_EXIST, username);
            GameMessage value = new GameMessage(msg, MessageStatus.OPERATION_ERR);
            MessageSender.sendMessage2Endpoint(value,this);
            return;
        }

        this.player = username;
        WEB_SOCKET_MANCALA_GAMER.put(player, this);
        GameMessage gameMessage = new GameMessage(username, MessageStatus.READY);
        MessageSender.sendMessage2Endpoints(gameMessage, WEB_SOCKET_MANCALA_GAMER.values());

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
            synchronized (WEB_SOCKET_MANCALA_GAMER) {
                Object[] players = WEB_SOCKET_MANCALA_GAMER.keySet().toArray();
                MancalaGame mancalaGame = new MancalaGame(players[0].toString(), players[1].toString());
                this.gameId = mancalaGame.getGameId();
                persistGamePlayers(mancalaGame);

                String message = MessageFormat.format(MancalaConstants.MSG_GAME_START, mancalaGame.getActiveBoardSegment().getPlayer());
                MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                GameMessage<MancalaGameVO> gameMessage = new GameMessage(message, mancalaGameVO, MessageStatus.START);
                MessageSender.sendMessage2Endpoints(gameMessage, GAME_PLAYERS_ENDPOINT.get(gameId));
            }
        }else{
            GameMessage<String> gameMessage = new GameMessage<>(MancalaConstants.MSG_PLAYER_IS_READY, MessageStatus.READY);
            MessageSender.sendMessage2Endpoints(gameMessage, WEB_SOCKET_MANCALA_GAMER.values());
        }

    }

    private void sowPits(GameRequestMessage rep){
        try {
            if(this.gameId == null){
                this.gameId = rep.getGameId();
            }
            Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
            if (optionalMancalaGame.isPresent()) {
                MancalaGame mancalaGame = optionalMancalaGame.get();
                mancalaGame.sow(new SowRequest(player, rep.getPitIdx()));
                MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                maintainGamePersistence(mancalaGame, mancalaGameVO);

                GameMessage sowGameMessage = getSowGameMessage(mancalaGame, rep.getPitIdx(), mancalaGameVO);
                List<MancalaEndpoint> mancalaEndpoints = GAME_PLAYERS_ENDPOINT.get(gameId);
                MessageSender.sendMessage2Endpoints(sowGameMessage, mancalaEndpoints);
            }

        } catch (MancalaGameException e) {
            GameMessage gameMessage = new GameMessage(e.getMessage(), MessageStatus.OPERATION_ERR);
            MessageSender.sendMessage2Endpoint(gameMessage,this);
        }
    }


    private void persistGamePlayers(MancalaGame mancalaGame) {
        gameRepository.save(mancalaGame);
        List<MancalaEndpoint> gameEndPoints = new ArrayList<>();
        gameEndPoints.addAll(WEB_SOCKET_MANCALA_GAMER.values());
        GAME_PLAYERS_ENDPOINT.put(mancalaGame.getGameId(), gameEndPoints);
        WEB_SOCKET_MANCALA_GAMER.clear();
    }

    private GameMessage getSowGameMessage(MancalaGame mancalaGame, int pitIdx, MancalaGameVO mancalaGameVO) {
        if (mancalaGame.isGameOver()) {
            return new GameMessage<>(mancalaGameVO.getGameInfo(), mancalaGameVO, MessageStatus.END);
        } else {
            MessageFormat.format(MancalaConstants.MSG_PLAYER_SOW_WITH_PIT_INDEX, player, pitIdx);
            return new GameMessage<>(MancalaConstants.MSG_PLAYER_SOW_WITH_PIT_INDEX, mancalaGameVO, MessageStatus.SOW);
        }
    }

    private static void maintainGamePersistence(MancalaGame mancalaGame, MancalaGameVO mancalaGameVO) {
        if (mancalaGameVO.isGameOver()) {
            gameRepository.deleteById(mancalaGameVO.getGameId());
        } else {
            gameRepository.save(mancalaGame);
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
        Collection<MancalaEndpoint> values = WEB_SOCKET_MANCALA_GAMER.values();
        return values.size() == 2;
    }

    public Session getSession(){
        return this.session;
    }


}
