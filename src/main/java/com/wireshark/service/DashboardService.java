package com.wireshark.service;

import com.wireshark.dto.AddressCount;
import com.wireshark.dto.ProtocolCount;
import com.wireshark.dto.TimeBucketCount;
import com.wireshark.repository.PacketHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Instant DEFAULT_START = Instant.EPOCH;
    private static final Instant DEFAULT_END = Instant.parse("2100-01-01T00:00:00Z");

    private final PacketHistoryRepository packetHistoryRepository;

    public DashboardService(PacketHistoryRepository packetHistoryRepository) {
        this.packetHistoryRepository = packetHistoryRepository;
    }

    public List<ProtocolCount> getProtocolDistribution(LocalDate dateDebut, LocalDate dateFin) {
        Instant start = toStartInstant(dateDebut);
        Instant end = toEndInstant(dateFin);
        return packetHistoryRepository.countByProtocolBetween(start, end).stream()
                .map(row -> new ProtocolCount((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<TimeBucketCount> getPacketsOverTime(LocalDate dateDebut, LocalDate dateFin) {
        Instant start = toStartInstant(dateDebut);
        Instant end = toEndInstant(dateFin);
        return packetHistoryRepository.countByDateBetween(start, end).stream()
                .map(row -> new TimeBucketCount(toInstant(row[0]), ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<AddressCount> getTop5Sources(LocalDate dateDebut, LocalDate dateFin) {
        Instant start = toStartInstant(dateDebut);
        Instant end = toEndInstant(dateFin);
        return packetHistoryRepository.top5SourcesBetween(start, end).stream()
                .map(row -> new AddressCount((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public List<AddressCount> getTop5Destinations(LocalDate dateDebut, LocalDate dateFin) {
        Instant start = toStartInstant(dateDebut);
        Instant end = toEndInstant(dateFin);
        return packetHistoryRepository.top5DestinationsBetween(start, end).stream()
                .map(row -> new AddressCount((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    private Instant toStartInstant(LocalDate date) {
        if (date == null) return DEFAULT_START;
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant toEndInstant(LocalDate date) {
        if (date == null) return DEFAULT_END;
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant toInstant(Object value) {
        if (value instanceof Instant i) return i;
        if (value instanceof java.sql.Timestamp ts) return ts.toInstant();
        if (value instanceof OffsetDateTime odt) return odt.toInstant();
        throw new IllegalArgumentException("Unsupported timestamp type: " + (value != null ? value.getClass() : "null"));
    }
}
