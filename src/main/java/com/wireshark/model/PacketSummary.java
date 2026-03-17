package com.wireshark.model;

public class PacketSummary {

    private long timestamp;
    private String protocol;
    private String srcIp;
    private String dstIp;
    private int srcPort;
    private int dstPort;
    private int length;

    public PacketSummary(String protocol, String srcIp, String dstIp,
            int srcPort, int dstPort, int length) {
        this.timestamp = System.currentTimeMillis();
        this.protocol = protocol;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.length = length;
    }

    public long getTimestamp() {
        return timestamp;
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

    @Override
    public String toString() {
        return String.format(
                "[%s] %s:%d  →  %s:%d  | %d octets",
                protocol, srcIp, srcPort, dstIp, dstPort, length);
    }
}
