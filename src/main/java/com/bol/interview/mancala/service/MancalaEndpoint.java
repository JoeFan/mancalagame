package com.bol.interview.mancala.service;

import com.bol.interview.mancala.constants.MancalaConstants;
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
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@ServerEndpoint(value = "/mancala/{username}")
@Slf4j
public class MancalaEndpoint {

    private static final BlockingQueue<MancalaEndpoint> WEB_SOCKET_MANCALA_GAMER = new LinkedBlockingQueue<>(MancalaConstants.MAX_WAITING_PLAYER);

    private static final Set<String> PLAYER_NAMES = new HashSet<>();
    private static final ConcurrentHashMap<String, List<MancalaEndpoint>> GAME_PLAYERS_ENDPOINT = new ConcurrentHashMap<>(MancalaConstants.MAX_GAME_SIZE);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static MancalaGameRepository gameRepository;
    private Session session;
    private String player;
    private String gameId;

    @Autowired
    public void setGameRepository(MancalaGameRepository mancalaGameRepository) {
        gameRepository = mancalaGameRepository;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        this.session = session;
        if (WEB_SOCKET_MANCALA_GAMER.size() == MancalaConstants.MAX_WAITING_PLAYER) {
            MessageSender.sendRoomIsFullMessage(this);
            return;
        }

        if (PLAYER_NAMES.contains(username)) {
            MessageSender.sendPlayerNameExistMsg(username, this);
            return;
        }

        this.player = username;
        PLAYER_NAMES.add(player);
        WEB_SOCKET_MANCALA_GAMER.add(this);
        MessageSender.sendPlayerIsReadyMessage(username, WEB_SOCKET_MANCALA_GAMER);
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
                List<MancalaEndpoint> gameEndPoints = new ArrayList<>(MancalaConstants.GAME_PLAYER_NUMBER);
                gameEndPoints.add(WEB_SOCKET_MANCALA_GAMER.poll());
                gameEndPoints.add(WEB_SOCKET_MANCALA_GAMER.poll());

                MancalaGame mancalaGame = createAndPersistGame(gameEndPoints);
                MessageSender.sendGameStartMessage(mancalaGame, gameEndPoints);
            }
        } else {
            MessageSender.sendPlayerIsReadyMessage(player, WEB_SOCKET_MANCALA_GAMER);
        }

    }

    private MancalaGame createAndPersistGame(List<MancalaEndpoint> gameEndPoints) {
        MancalaGame mancalaGame = new MancalaGame(gameEndPoints.get(0).getPlayer(), gameEndPoints.get(1).getPlayer());
        this.gameId = mancalaGame.getGameId();
        GAME_PLAYERS_ENDPOINT.put(gameId, gameEndPoints);
        gameRepository.save(mancalaGame);
        return mancalaGame;
    }

    private String getPlayer() {
        return player;
    }

    private void sowPits(GameRequestMessage gameRequestMessage) {
        initGameId(gameRequestMessage);
        try {
            sowGamePit(gameRequestMessage);
        } catch (MancalaGameException e) {
            MessageSender.sendOperationErrorMessage(e.getMessage(), this);
        }
    }

    private void sowGamePit(GameRequestMessage gameRequestMessage) {
        Optional<MancalaGame> optionalMancalaGame = gameRepository.findById(gameId);
        if (optionalMancalaGame.isPresent()) {
            MancalaGame mancalaGame = optionalMancalaGame.get();
            mancalaGame.sow(new SowRequest(player, gameRequestMessage.getPitIdx()));
            MancalaGameVO mancalaGameVO = new MancalaGameVO(mancalaGame);
            persistGame(mancalaGame, mancalaGameVO);
            MessageSender.sendSowedResultMessage(gameRequestMessage, mancalaGameVO, GAME_PLAYERS_ENDPOINT.get(gameId));

        }
    }

    private static void persistGame(MancalaGame mancalaGame, MancalaGameVO mancalaGameVO) {
        if (mancalaGameVO.isGameOver()) {
            gameRepository.deleteById(mancalaGameVO.getGameId());
        } else {
            gameRepository.save(mancalaGame);
        }
    }

    private void initGameId(GameRequestMessage gameRequestMessage) {
        if (!Optional.ofNullable(gameId).isPresent()) {
            this.gameId = gameRequestMessage.getGameId();
        }
    }

    @OnClose
    public void onClose() {
        if (Optional.of(gameId).isPresent()) {
            GAME_PLAYERS_ENDPOINT.remove(this.gameId);
        }
        if (Optional.of(player).isPresent()) {
            PLAYER_NAMES.remove(this.player);
        }
        if (this.session.isOpen()) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("session close error", e);
            }
        }

    }


    public boolean isPrepareAll() {
        return WEB_SOCKET_MANCALA_GAMER.size() >= 2;
    }

    public Session getSession() {
        return this.session;
    }


}
