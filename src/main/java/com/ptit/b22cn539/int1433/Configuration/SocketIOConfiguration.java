package com.ptit.b22cn539.int1433.Configuration;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ptit.b22cn539.int1433.DTO.Music.MusicResponse;
import com.ptit.b22cn539.int1433.DTO.User.UserResponse;
import com.ptit.b22cn539.int1433.Mapper.MusicMapper;
import com.ptit.b22cn539.int1433.Models.AnswerEntity;
import com.ptit.b22cn539.int1433.Models.GameEntity;
import com.ptit.b22cn539.int1433.Models.GameItemEntity;
import com.ptit.b22cn539.int1433.Models.MusicEntity;
import com.ptit.b22cn539.int1433.Models.SessionUserEntity;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Repository.IGameItemRepository;
import com.ptit.b22cn539.int1433.Repository.IGameRepository;
import com.ptit.b22cn539.int1433.Repository.IMusicRepository;
import com.ptit.b22cn539.int1433.Repository.ISessionUserRepository;
import com.ptit.b22cn539.int1433.Repository.IUserRepository;
import com.ptit.b22cn539.int1433.Service.User.IUserService;
import com.ptit.b22cn539.int1433.Utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SocketIOConfiguration {
    @Value("${socket-server.host}")
    String host;
    @Value("${socket-server.port}")
    Integer port;
    final IUserService userService;
    final JwtUtils jwtUtils;
    SocketIOServer server;
    final ISessionUserRepository sessionUserRepository;
    final IMusicRepository musicRepository;
    final MusicMapper musicMapper;
    final IGameRepository gameRepository;
    final IGameItemRepository gameItemRepository;
    final IUserRepository userRepository;

    // cho vào header và auth đều không dùng được bị lỗi CORS -> chưa hiểu tại sao
    // lý do GPT đưa ra là do khi thực hiện CORS thì header và auth không được gửi đi
    // nên phải dùng url param để truyền token

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(this.host);
        config.setPort(this.port);
        config.setEnableCors(true);
        config.setOrigin("*");
        config.setAllowHeaders("*");
        config.setAuthorizationListener(this.handleAuthorization());
        this.server = new SocketIOServer(config);
        this.server.addListeners(this);
        this.server.start();
        return this.server;
    }

    @OnConnect
    public void onConnect(SocketIOClient socketIOClient) {
        log.info("Connected: {}", socketIOClient.getSessionId());
        AuthorizationResult authorizationResult = this.server.getConfiguration().getAuthorizationListener().getAuthorizationResult(socketIOClient.getHandshakeData());
        Map<String, Object> data = authorizationResult.getStoreParams();
        log.info("Authorization result: {}", data);
        String username = data.get("sub").toString();
        this.sessionUserRepository.deleteByUsername(username);
        SessionUserEntity sessionUser = SessionUserEntity.builder()
                .sessionId(socketIOClient.getSessionId().toString())
                .username(username)
                .build();
        UserEntity userEntity = this.userRepository.findByUsername(username);
        this.sessionUserRepository.save(sessionUser);
        this.server.getBroadcastOperations().sendEvent("topic/changeStatus", socketIOClient, Map.of("id", userEntity.getId(), "fullName", userEntity.getFullName(),"username", username, "status", UserStatus.ONLINE));
    }

    @OnEvent("topic/getAllUsersResponse")
    public void getAllUsers(SocketIOClient socketIOClient) {
        SessionUserEntity sessionUser = this.sessionUserRepository.findBySessionId(socketIOClient.getSessionId().toString());
        if (sessionUser == null) {
            socketIOClient.sendEvent("error", "Session not ready yet");
            return;
        }
        String username = sessionUser.getUsername();
        List<UserResponse> users = this.userService.getAllUsers(username);
        socketIOClient.sendEvent("topic/getAllUsersResponse", users);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient socketIOClient) {
        log.info("Disconnected: {}", socketIOClient.getSessionId());
        SessionUserEntity sessionUser = this.sessionUserRepository.findBySessionId(socketIOClient.getSessionId().toString());
        if (sessionUser != null) {
            AuthorizationResult authorizationResult = this.server.getConfiguration().getAuthorizationListener().getAuthorizationResult(socketIOClient.getHandshakeData());
            Map<String, Object> data = authorizationResult.getStoreParams();
            String username = data.get("sub").toString();
            UserEntity userEntity = this.userRepository.findByUsername(username);
            this.sessionUserRepository.delete(sessionUser);
            this.server.getBroadcastOperations().sendEvent("topic/changeStatus", socketIOClient, Map.of("id", userEntity.getId(), "fullName", userEntity.getFullName(),"username", username, "status", UserStatus.OFFLINE));
        }
    }

    @OnEvent(value = "topic/inviteUser")
    public void inviteUser(SocketIOClient fromClient, @ProjectedPayload String toUsername) {
        SessionUserEntity toSessionUser = this.sessionUserRepository.findByUsername(toUsername);
        if (toSessionUser != null) {
            SocketIOClient toClient = this.server.getClient(UUID.fromString(toSessionUser.getSessionId()));
            String fromUserId = fromClient.getSessionId().toString();
            SessionUserEntity fromSessionUser = this.sessionUserRepository.findBySessionId(fromUserId);
            if (toClient != null && fromSessionUser != null) {
                toClient.sendEvent("topic/inviteUser", Map.of("from", fromSessionUser.getUsername()));
            }
        }
    }

    @OnEvent(value = "topic/acceptInvite")
    public void acceptInvite(SocketIOClient fromClient, @ProjectedPayload String toUsername) {
        SessionUserEntity toSessionUser = this.sessionUserRepository.findByUsername(toUsername);
        if (toSessionUser != null) {
            SocketIOClient toClient = this.server.getClient(UUID.fromString(toSessionUser.getSessionId()));
            String fromUserSessionId = fromClient.getSessionId().toString();
            SessionUserEntity fromSessionUser = this.sessionUserRepository.findBySessionId(fromUserSessionId);
            if (toClient != null && fromSessionUser != null) {
                toClient.sendEvent("topic/acceptInvite", Map.of("from", fromSessionUser.getUsername()));
                List<MusicEntity> musics = this.musicRepository.findRandom10Music();
                List<MusicResponse> musicResponses = musics.stream().map(this.musicMapper::toMusicResponse).toList();
                log.info("Init game with musics: {}", musicResponses);
                GameEntity game1 = new GameEntity();
                GameEntity game2 = new GameEntity();
                String matchCode = UUID.randomUUID().toString();
                game1.setMatchCode(matchCode);
                game2.setMatchCode(matchCode);
                game1.setUsername(fromSessionUser.getUsername());
                game2.setUsername(toSessionUser.getUsername());
                this.gameRepository.saveAll(List.of(game1, game2));
                List<Long> musicIds = musics.stream().map(MusicEntity::getId).toList();
                toClient.sendEvent("topic/initGame", Map.of("musicIds", musicIds, "gameId", game2.getId(), "matchCode", matchCode));
                fromClient.sendEvent("topic/initGame", Map.of("musicIds", musicIds, "gameId", game1.getId(), "matchCode", matchCode));
            }
        }
    }

    @OnEvent(value = "topic/changeQuestion")
    public void handleChangeQuestion(@ProjectedPayload Map<String, String> data, SocketIOClient fromClient) {
        Long musicId = Long.valueOf(data.get("musicId"));
        MusicEntity music = this.musicRepository.findById(musicId).orElseThrow();
        MusicResponse musicResponse = this.musicMapper.toMusicResponse(music);
        fromClient.sendEvent("topic/changeQuestion", musicResponse);
    }

    @OnEvent(value = "topic/handleAnswer")
    public void handleAnswer(SocketIOClient fromClient, @ProjectedPayload Map<String, String> payload) {
        String answer = payload.get("answerId");
        Long musicId = Long.valueOf(payload.get("musicId"));
        Long gameId = Long.valueOf(payload.get("gameId"));
        GameItemEntity gameItem = new GameItemEntity();
        GameEntity gameEntity = this.gameRepository.findById(gameId).orElseThrow();
        gameItem.setGame(gameEntity);
        MusicEntity musicEntity = this.musicRepository.findById(musicId).orElseThrow();
        gameItem.setMusic(musicEntity);
        if (!StringUtils.hasText(answer)) {
            gameItem.setAnswerId(null);
            this.gameItemRepository.save(gameItem);
            fromClient.sendEvent("topic/answerResult", Map.of("score", 0));
            return;
        }
        Long answerId = Long.valueOf(answer);
        List<AnswerEntity> answers = musicEntity.getAnswers();
        Long correctAnswerId = answers.stream().filter(AnswerEntity::isCorrect).findFirst().orElseThrow().getId();
        gameItem.setAnswerId(answerId);
        int score = 0;
        if (answerId.equals(correctAnswerId)) {
            score += 1;
        }
        this.gameItemRepository.save(gameItem);
        fromClient.sendEvent("topic/answerResult", Map.of("score", score));
    }

    @OnEvent(value = "topic/finishGame")
    public void finishGame(@ProjectedPayload Map<String, String> data, SocketIOClient fromClient) {
        String fromUserSessionId = fromClient.getSessionId().toString();
        SessionUserEntity fromSessionUser = this.sessionUserRepository.findBySessionId(fromUserSessionId);
        if (fromSessionUser != null) {
            Long gameId = Long.valueOf(data.get("gameId"));
            GameEntity game = this.gameRepository.findById(gameId).orElseThrow();
            int totalScore = Integer.parseInt(data.get("score"));
            game.setScoreUser(totalScore);
            this.gameRepository.save(game);
            fromClient.sendEvent("topic/finishGame", Map.of("totalScore", totalScore));
        }
    }

    @OnEvent("topic/getResult")
    public void getResult(@ProjectedPayload Map<String, String> data, SocketIOClient fromClient) {
        String matchCode = data.get("matchCode");
        List<GameEntity> gameEntities = this.gameRepository.findByMatchCode(matchCode);
        GameEntity game = gameEntities.stream()
                .filter(g -> g.getUsername().equals(this.sessionUserRepository.findBySessionId(fromClient.getSessionId().toString()).getUsername()))
                .findFirst()
                .orElseThrow();
        GameEntity opponentGame = gameEntities.stream()
                .filter(g -> !g.getId().equals(game.getId()))
                .findFirst()
                .orElseThrow();
        if (opponentGame.getScoreUser() > game.getScoreUser()) {
            fromClient.sendEvent("topic/getResult", Map.of("result", "LOSE", "yourScore", game.getScoreUser(), "opponentScore", opponentGame.getScoreUser()));
        } else if (opponentGame.getScoreUser() < game.getScoreUser()) {
            fromClient.sendEvent("topic/getResult", Map.of("result", "WIN", "yourScore", game.getScoreUser(), "opponentScore", opponentGame.getScoreUser()));
        } else {
            fromClient.sendEvent("topic/getResult", Map.of("result", "DRAW", "yourScore", game.getScoreUser(), "opponentScore", opponentGame.getScoreUser()));
        }
    }

    public AuthorizationListener handleAuthorization() {
        return handshakeData -> {
            String token = handshakeData.getSingleUrlParam("token");
            if (!StringUtils.hasText(token)) return new AuthorizationResult(false);
            log.info(token);
            Claims claims = this.jwtUtils.extractClaims(token);
            return new AuthorizationResult(true, claims);
        };
    }

    @OnEvent("topic/getTopRanking")
    public void getTopRanking(SocketIOClient fromClient) {
        List<Object[]> result = gameRepository.topRankingRaw();
        fromClient.sendEvent("topic/getTopRanking", result);
    }

    @PreDestroy
    public void stopServer() {
        if (this.server != null) {
            this.server.stop();
            this.sessionUserRepository.deleteAll();
        }
    }
}