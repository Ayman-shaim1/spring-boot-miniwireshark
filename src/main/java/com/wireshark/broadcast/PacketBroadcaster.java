package com.wireshark.broadcast;

import com.corundumstudio.socketio.SocketIOServer;
import com.wireshark.model.PacketSummary;
import org.springframework.stereotype.Component;

@Component
public class PacketBroadcaster {

    private static final String PACKET_EVENT = "packet";

    private final SocketIOServer socketServer;

    public PacketBroadcaster(SocketIOServer socketServer) {
        this.socketServer = socketServer;
    }

    /**
     * Envoie un PacketSummary à tous les clients Socket.IO connectés.
     */
    public void broadcast(PacketSummary summary) {
        socketServer.getBroadcastOperations().sendEvent(PACKET_EVENT, summary);
    }
}
