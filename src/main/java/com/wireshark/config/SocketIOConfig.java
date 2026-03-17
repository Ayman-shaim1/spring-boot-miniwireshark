package com.wireshark.config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Value("${socketio.port:9092}")
    private int socketPort;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(socketPort);
        config.setOrigin("*");
        config.getSocketConfig().setReuseAddress(true);

        server = new SocketIOServer(config);
        server.start();
        return server;
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
