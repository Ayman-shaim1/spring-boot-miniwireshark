package com.wireshark.model;

public class PacketSummary {

    private long no;
    private long timestamp;
    private String protocol;
    private String srcIp;
    private String dstIp;
    private int srcPort;
    private int dstPort;
    private int length;
    private String info;

    public PacketSummary(long no, String protocol, String srcIp, String dstIp,
            int srcPort, int dstPort, int length, String info) {
        this.no = no;
        this.timestamp = System.currentTimeMillis();
        this.protocol = protocol;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.length = length;
        this.info = info != null && !info.isBlank() ? info : "-";
    }

    public long getNo() {
        return no;
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

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %s:%d  →  %s:%d  | %d octets",
                protocol, srcIp, srcPort, dstIp, dstPort, length);
    }
}
