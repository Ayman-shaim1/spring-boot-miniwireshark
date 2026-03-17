package com.wireshark.service;

import com.wireshark.broadcast.PacketBroadcaster;
import com.wireshark.model.PacketSummary;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PacketCaptureService {

    private final PacketBroadcaster broadcaster;

    public PacketCaptureService(PacketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    private PcapHandle handle;
    private Thread captureThread;
    private volatile boolean running = false;

    public List<String> listInterfaces() throws PcapNativeException {
        return Pcaps.findAllDevs()
                .stream()
                .map(PcapNetworkInterface::getName)
                .collect(Collectors.toList());
    }

    public void start(String interfaceName) throws PcapNativeException, NotOpenException {

        PcapNetworkInterface nif = Pcaps.getDevByName(interfaceName);
        if (nif == null) {
            throw new IllegalArgumentException("Interface introuvable : " + interfaceName);
        }

        handle = nif.openLive(
                65536,
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                10);

        running = true;

        captureThread = new Thread(() -> {
            try {
                handle.loop(-1, (PacketListener) this::onPacketReceived);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (PcapNativeException | NotOpenException e) {
                System.err.println("Erreur capture : " + e.getMessage());
            }
        }, "pcap-thread");

        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void stop() {
        running = false;
        if (handle != null && handle.isOpen()) {
            try {
                handle.breakLoop();
            } catch (NotOpenException e) {
                System.err.println("Handle déjà fermé.");
            }
            handle.close();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void onPacketReceived(Packet packet) {
        if (!running)
            return;

        String srcIp = "inconnu";
        String dstIp = "inconnu";
        int srcPort = 0;
        int dstPort = 0;
        String protocol = "AUTRE";

        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 != null) {
            srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
            dstIp = ipv4.getHeader().getDstAddr().getHostAddress();
        }

        TcpPacket tcp = packet.get(TcpPacket.class);
        if (tcp != null) {
            srcPort = tcp.getHeader().getSrcPort().valueAsInt();
            dstPort = tcp.getHeader().getDstPort().valueAsInt();
            protocol = detectProtocol(dstPort);
        }

        UdpPacket udp = packet.get(UdpPacket.class);
        if (udp != null) {
            srcPort = udp.getHeader().getSrcPort().valueAsInt();
            dstPort = udp.getHeader().getDstPort().valueAsInt();
            protocol = (dstPort == 53 || srcPort == 53) ? "DNS" : "UDP";
        }
        PacketSummary summary = new PacketSummary(
                protocol, srcIp, dstIp, srcPort, dstPort, packet.length());
        broadcaster.broadcast(summary);
    }

    private String detectProtocol(int port) {
        return switch (port) {
            case 80 -> "HTTP";
            case 443 -> "HTTPS";
            case 22 -> "SSH";
            case 25, 587 -> "SMTP";
            case 53 -> "DNS";
            case 3306 -> "MYSQL";
            case 5432 -> "POSTGRESQL";
            default -> "TCP";
        };
    }
}
