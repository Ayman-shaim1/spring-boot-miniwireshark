package com.wireshark.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "packet_history")
public class PacketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long packetNo;
    private long timestamp;

    @Column(name = "captured_at")
    private Instant capturedAt;
    private String protocol;
    private String srcIp;
    private String dstIp;
    private int srcPort;
    private int dstPort;
    private int length;

    @Column(length = 2048)
    private String info;

    public PacketHistory() {
    }

    public PacketHistory(Long packetNo, long timestamp, Instant capturedAt, String protocol, String srcIp, String dstIp,
                         int srcPort, int dstPort, int length, String info) {
        this.packetNo = packetNo;
        this.timestamp = timestamp;
        this.capturedAt = capturedAt != null ? capturedAt : Instant.now();
        this.protocol = protocol;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.length = length;
        this.info = info != null && !info.isBlank() ? info : "-";
    }

    public Long getId() {
        return id;
    }

    public Long getPacketNo() {
        return packetNo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getLength() {
        return length;
    }

    public String getInfo() {
        return info;
    }
}
