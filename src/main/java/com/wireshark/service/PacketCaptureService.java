package com.wireshark.service;

import com.wireshark.broadcast.PacketBroadcaster;
import com.wireshark.entity.PacketHistory;
import com.wireshark.model.PacketSummary;
import com.wireshark.repository.PacketHistoryRepository;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class PacketCaptureService {

    private final PacketBroadcaster broadcaster;
    private final PacketHistoryRepository packetHistoryRepository;

    public PacketCaptureService(PacketBroadcaster broadcaster, PacketHistoryRepository packetHistoryRepository) {
        this.broadcaster = broadcaster;
        this.packetHistoryRepository = packetHistoryRepository;
    }

    private PcapHandle handle;
    private Thread captureThread;
    private volatile boolean running = false;
    private final AtomicLong packetCounter = new AtomicLong(0);

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
        packetCounter.set(0);

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
        String info = "-";

        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        IpV6Packet ipv6 = packet.get(IpV6Packet.class);
        if (ipv4 != null) {
            srcIp = ipv4.getHeader().getSrcAddr().getHostAddress();
            dstIp = ipv4.getHeader().getDstAddr().getHostAddress();
        } else if (ipv6 != null) {
            srcIp = ipv6.getHeader().getSrcAddr().getHostAddress();
            dstIp = ipv6.getHeader().getDstAddr().getHostAddress();
        }

        TcpPacket tcp = packet.get(TcpPacket.class);
        if (tcp != null) {
            var header = tcp.getHeader();
            srcPort = header.getSrcPort().valueAsInt();
            dstPort = header.getDstPort().valueAsInt();
            protocol = detectProtocol(dstPort);
            int payloadLen = Math.max(0, tcp.length() - header.length());
            info = buildTcpInfo(header, srcPort, dstPort, payloadLen);
        }

        UdpPacket udp = packet.get(UdpPacket.class);
        if (udp != null) {
            srcPort = udp.getHeader().getSrcPort().valueAsInt();
            dstPort = udp.getHeader().getDstPort().valueAsInt();
            protocol = (dstPort == 53 || srcPort == 53) ? "DNS" : "UDP";
            int udpPayloadLen = Math.max(0, udp.length() - 8);
            info = srcPort + " → " + dstPort + " Len=" + udpPayloadLen;
        }

        if (tcp == null && udp == null) {
            info = srcIp.equals("inconnu") ? "-" : (srcPort > 0 ? srcPort + " → " + dstPort : srcIp + " → " + dstIp);
        }

        PacketSummary summary = new PacketSummary(
                packetCounter.incrementAndGet(), protocol, srcIp, dstIp, srcPort, dstPort, packet.length(), info);
        broadcaster.broadcast(summary);
        packetHistoryRepository.save(toHistory(summary));
    }

    private PacketHistory toHistory(PacketSummary s) {
        Instant capturedAt = Instant.now();
        return new PacketHistory(s.getNo(), s.getTimestamp(), capturedAt, s.getProtocol(), s.getSrcIp(), s.getDstIp(),
                s.getSrcPort(), s.getDstPort(), s.getLength(), s.getInfo());
    }

    private String buildTcpInfo(org.pcap4j.packet.TcpPacket.TcpHeader header, int srcPort, int dstPort, int payloadLen) {
        String flags = buildTcpFlags(header);
        long seq = header.getSequenceNumberAsLong() & 0xFFFFFFFFL;
        long ack = header.getAcknowledgmentNumberAsLong() & 0xFFFFFFFFL;
        int win = header.getWindowAsInt() & 0xFFFF;

        String base = srcPort + " → " + dstPort + " [" + flags + "] Seq=" + seq + " Ack=" + ack + " Win=" + win + " Len=" + payloadLen;

        if (payloadLen > 0 && (srcPort == 443 || dstPort == 443)) {
            return payloadLen + " Application Data";
        }
        if (payloadLen == 0 && header.getAck() && win > 0) {
            return "[TCP Window Update] " + base;
        }
        return base;
    }

    private String buildTcpFlags(org.pcap4j.packet.TcpPacket.TcpHeader header) {
        var parts = new java.util.ArrayList<String>();
        if (header.getUrg()) parts.add("URG");
        if (header.getAck()) parts.add("ACK");
        if (header.getPsh()) parts.add("PSH");
        if (header.getRst()) parts.add("RST");
        if (header.getSyn()) parts.add("SYN");
        if (header.getFin()) parts.add("FIN");
        return parts.isEmpty() ? "-" : String.join(" ", parts);
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
