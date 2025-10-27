package com.ptit.b22cn539.int1433.Configuration;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.ptit.b22cn539.int1433.Controller.SocketIO.UserSocketIOController;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Service.User.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(this.host);
        config.setPort(this.port);
        config.setEnableCors(true);
        config.setOrigin("*");
        SocketIOServer server = new SocketIOServer(config);
        server.addListeners(this);
        server.addListeners(this.userSocketIOController);
        server.start();
        return server;
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
}
