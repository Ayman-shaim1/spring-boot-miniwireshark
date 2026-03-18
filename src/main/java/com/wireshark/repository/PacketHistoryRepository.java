package com.wireshark.repository;

import com.wireshark.entity.PacketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PacketHistoryRepository extends JpaRepository<PacketHistory, Long>, JpaSpecificationExecutor<PacketHistory> {

    @Query("SELECT ph.protocol, COUNT(ph) FROM PacketHistory ph WHERE ph.capturedAt >= :start AND ph.capturedAt < :end GROUP BY ph.protocol")
    List<Object[]> countByProtocolBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = "SELECT date_trunc('day', ph.captured_at) AS time, COUNT(*) AS cnt FROM packet_history ph " +
            "WHERE ph.captured_at >= :start AND ph.captured_at < :end " +
            "GROUP BY date_trunc('day', ph.captured_at) ORDER BY time", nativeQuery = true)
    List<Object[]> countByDateBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = "SELECT ph.src_ip || ':' || ph.src_port AS address, COUNT(*) AS cnt FROM packet_history ph " +
            "WHERE ph.captured_at >= :start AND ph.captured_at < :end " +
            "GROUP BY ph.src_ip, ph.src_port ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
    List<Object[]> top5SourcesBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = "SELECT ph.dst_ip || ':' || ph.dst_port AS address, COUNT(*) AS cnt FROM packet_history ph " +
            "WHERE ph.captured_at >= :start AND ph.captured_at < :end " +
            "GROUP BY ph.dst_ip, ph.dst_port ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
    List<Object[]> top5DestinationsBetween(@Param("start") Instant start, @Param("end") Instant end);
}
