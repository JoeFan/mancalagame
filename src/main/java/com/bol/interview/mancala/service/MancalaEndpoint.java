package com.bol.interview.mancala.service;

import com.bol.interview.mancala.exception.MancalaGameException;
import com.bol.interview.mancala.model.MancalaGame;
import com.bol.interview.mancala.repository.MancalaGameRepository;
import com.bol.interview.mancala.request.SowRequest;
import com.bol.interview.mancala.util.MessageSender;
import com.bol.interview.mancala.vo.GameRequestMessage;
import com.bol.interview.mancala.vo.MancalaGameVO;
import com.bol.interview.mancala.vo.PlayerAction;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/mancala/{username}")
@Slf4j
public class MancalaEndpoint {

    private static final ConcurrentHashMap<String, MancalaEndpoint> WEB_SOCKET_MANCALA_GAMER = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<MancalaEndpoint>> GAME_PLAYERS_ENDPOINT = new ConcurrentHashMap<>();
    private static final int LENGTH = 2;
    private static ObjectMapper mapper = new ObjectMapper();
    private static MancalaGameRepository gameRepository;
    private Session session;
    private String player;
    private String gameId;

    private static void persistGameAndPlayers(MancalaGame mancalaGame) {
        List<MancalaEndpoint> gameEndPoints = new ArrayList<>();
        gameEndPoints.addAll(WEB_SOCKET_MANCALA_GAMER.values());
        GAME_PLAYERS_ENDPOINT.put(mancalaGame.getGameId(), gameEndPoints);
        WEB_SOCKET_MANCALA_GAMER.clear();
    }

    private static void maintainGamePersistence(MancalaGame mancalaGame, MancalaGameVO mancalaGameVO) {
        if (mancalaGameVO.isGameOver()) {
            gameRepository.deleteById(mancalaGameVO.getGameId());
        } else {
            gameRepository.save(mancalaGame);
        }
    }

    @Autowired
    public void setGameRepository(MancalaGameRepository mancalaGameRepository) {
        gameRepository = mancalaGameRepository;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        if (WEB_SOCKET_MANCALA_GAMER.size() == LENGTH) {
            MessageSender.sendRoomIsFullMessage(this);
            return;
        }

        if (WEB_SOCKET_MANCALA_GAMER.containsKey(username)) {
            MessageSender.sendPlayerNameExistMsg(username, this);
            return;
        }

        this.player = username;
        WEB_SOCKET_MANCALA_GAMER.put(player, this);
        MessageSender.sendPlayerIsReadyMessage(username, WEB_SOCKET_MANCALA_GAMER.values());
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
                MessageSender.sendGameStartMessage(mancalaGame, GAME_PLAYERS_ENDPOINT.get(gameId));
            }
        } else {
            MessageSender.sendPlayerIsReadyMessage(player, WEB_SOCKET_MANCALA_GAMER.values());
        }

    }

    private void sowPits(GameRequestMessage gameRequestMessage) {
        try {
            if (this.gameId == null) {
                this.gameId = gameRequestMessage.getGameId();
            }
            Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
            if (optionalMancalaGame.isPresent()) {
                MancalaGame mancalaGame = optionalMancalaGame.get();
                mancalaGame.sow(new SowRequest(player, gameRequestMessage.getPitIdx()));
                MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
                maintainGamePersistence(mancalaGame, mancalaGameVO);
                MessageSender.sendSowedResultMessage(gameRequestMessage, mancalaGameVO, GAME_PLAYERS_ENDPOINT.get(gameId));

            }

        } catch (MancalaGameException e) {
            MessageSender.sendOperationErrorMessage(e.getMessage(), this);
        }
    }

    private void persistGamePlayers(MancalaGame mancalaGame) {
        gameRepository.save(mancalaGame);
        persistGameAndPlayers(mancalaGame);
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


    public boolean isPrepareAll() {
        Collection<MancalaEndpoint> values = WEB_SOCKET_MANCALA_GAMER.values();
        return values.size() == 2;
    }

    public Session getSession() {
        return this.session;
    }


}
