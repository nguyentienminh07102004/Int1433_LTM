package com.ptit.b22cn539.int1433.Configuration;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ptit.b22cn539.int1433.DTO.User.UserResponse;
import com.ptit.b22cn539.int1433.Models.SessionUserEntity;
import com.ptit.b22cn539.int1433.Repository.ISessionUserRepository;
import com.ptit.b22cn539.int1433.Service.User.IUserService;
import com.ptit.b22cn539.int1433.Utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        SessionUserEntity sessionUser = SessionUserEntity.builder()
                .sessionId(socketIOClient.getSessionId().toString())
                .username(username)
                .build();
        this.sessionUserRepository.save(sessionUser);
        List<UserResponse> users = this.userService.getAllUsers(username);
        socketIOClient.sendEvent("topic/getAllUsersResponse", users);
        this.server.getBroadcastOperations().sendEvent("topic/changeStatus", socketIOClient, Map.of("username", username, "status", UserStatus.ONLINE));
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient socketIOClient) {
        log.info("Disconnected: {}", socketIOClient.getSessionId());
        SessionUserEntity sessionUser = this.sessionUserRepository.findBySessionId(socketIOClient.getSessionId().toString());
        if (sessionUser != null) {
            this.sessionUserRepository.delete(sessionUser);
            this.server.getBroadcastOperations().sendEvent("topic/changeStatus", socketIOClient, Map.of("username", sessionUser.getUsername(), "status", UserStatus.OFFLINE));
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

    public AuthorizationListener handleAuthorization() {
        return handshakeData -> {
            String token = handshakeData.getSingleUrlParam("token");
            if (!StringUtils.hasText(token)) return new AuthorizationResult(false);
            log.info(token);
            Claims claims = this.jwtUtils.extractClaims(token);
            return new AuthorizationResult(true, claims);
        };
    }

    @PreDestroy
    public void stopServer() {
        if (this.server != null) {
            this.server.stop();
            this.sessionUserRepository.deleteAll();
        }
    }
}
