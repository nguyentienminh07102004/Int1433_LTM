package com.ptit.b22cn539.int1433.Configuration;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.ptit.b22cn539.int1433.Controller.SocketIO.UserSocketIOController;
import com.ptit.b22cn539.int1433.Models.UserEntity;
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
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SocketIOConfiguration {
    @Value("${socket-server.host}")
    String host;
    @Value("${socket-server.port}")
    Integer port;
    final UserSocketIOController userSocketIOController;
    final IUserService userService;
    final JwtUtils jwtUtils;
    SocketIOServer server;

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
        this.server.addListeners(this.userSocketIOController);
        this.server.start();
        return this.server;
    }

    @OnConnect
    public void onConnect(SocketIOClient socketIOClient) {
        log.info("Connected: {}", socketIOClient.getSessionId());
        List<UserEntity> users = this.userService.getAllUsers();
        socketIOClient.sendEvent("topic/getAllUsersResponse", users);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient socketIOClient) {
        log.info("Disconnected: {}", socketIOClient.getSessionId());
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
        }
    }
}
