package com.ptit.b22cn539.int1433.Configuration;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SocketIOConfiguration {
    @Value("${socket-server.host}")
    String host;
    @Value("${socket-server.port}")
    Integer port;


    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setEnableCors(false);
        SocketIOServer server = new SocketIOServer(config);
        server.addListeners(this);
        server.start();
        return server;
    }

    @OnConnect
    public void onConnect(SocketIOClient socketIOClient) {
        log.info("Connected: {}", socketIOClient.getSessionId());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient socketIOClient) {
        log.info("Disconnected: {}", socketIOClient.getSessionId());
    }
}
