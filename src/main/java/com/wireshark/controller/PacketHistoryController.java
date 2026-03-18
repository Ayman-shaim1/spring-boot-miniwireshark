package com.wireshark.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wireshark.entity.PacketHistory;
import com.wireshark.repository.PacketHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@Tag(name = "Historique", description = "Historique des paquets capturés")
public class PacketHistoryController {

    private static final int EXPORT_MAX_ROWS = 50_000;

    private final PacketHistoryRepository packetHistoryRepository;
    private final ObjectMapper objectMapper;

    public PacketHistoryController(PacketHistoryRepository packetHistoryRepository, ObjectMapper objectMapper) {
        this.packetHistoryRepository = packetHistoryRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @Operation(summary = "Liste l'historique", description = "Retourne l'historique des paquets capturés avec filtres (paginé)")
    public ResponseEntity<List<PacketHistory>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) Integer length,
            @RequestParam(required = false) String info,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        var pageable = PageRequest.of(page, Math.min(size, 500), Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<PacketHistory> spec = buildSpecification(source, destination, protocol, length, info, dateDebut,
                dateFin);
        var result = packetHistoryRepository.findAll(spec, pageable);
        return ResponseEntity.ok(result.getContent());
    }

    @GetMapping("/export")
    @Operation(summary = "Exporter l'historique", description = "Exporte l'historique en JSON ou CSV (fichier téléchargeable, filtres identiques à la liste)")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) Integer length,
            @RequestParam(required = false) String info,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin)
            throws JsonProcessingException {
        var pageable = PageRequest.of(0, EXPORT_MAX_ROWS, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<PacketHistory> spec = buildSpecification(source, destination, protocol, length, info, dateDebut,
                dateFin);
        List<PacketHistory> data = packetHistoryRepository.findAll(spec, pageable).getContent();

        byte[] content;
        String filename;
        String contentType;

        if ("csv".equalsIgnoreCase(format)) {
            content = toCsv(data);
            filename = "packet-history.csv";
            contentType = "text/csv; charset=UTF-8";
        } else {
            content = objectMapper.writeValueAsBytes(data);
            filename = "packet-history.json";
            contentType = "application/json";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok().headers(headers).body(content);
    }

    private byte[] toCsv(List<PacketHistory> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM for Excel
        sb.append("id,packetNo,timestamp,capturedAt,protocol,srcIp,dstIp,srcPort,dstPort,length,info\n");
        for (PacketHistory p : data) {
            sb.append(p.getId()).append(",")
                    .append(p.getPacketNo()).append(",")
                    .append(p.getTimestamp()).append(",")
                    .append(p.getCapturedAt() != null ? p.getCapturedAt().toString() : "").append(",")
                    .append(escapeCsv(p.getProtocol())).append(",")
                    .append(escapeCsv(p.getSrcIp())).append(",")
                    .append(escapeCsv(p.getDstIp())).append(",")
                    .append(p.getSrcPort()).append(",")
                    .append(p.getDstPort()).append(",")
                    .append(p.getLength()).append(",")
                    .append(escapeCsv(p.getInfo())).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String s) {
        if (s == null)
            return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private Specification<PacketHistory> buildSpecification(String source, String destination,
            String protocol, Integer length, String info,
            LocalDate dateDebut, LocalDate dateFin) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (source != null && !source.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("srcIp")), "%" + source.toLowerCase() + "%"));
            }
            if (destination != null && !destination.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("dstIp")), "%" + destination.toLowerCase() + "%"));
            }
            if (protocol != null && !protocol.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("protocol")), "%" + protocol.toLowerCase() + "%"));
            }
            if (length != null && length > 0) {
                predicates.add(cb.equal(root.get("length"), length));
            }
            if (info != null && !info.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("info")), "%" + info.toLowerCase() + "%"));
            }
            ZoneId zone = ZoneId.systemDefault();
            if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capturedAt"),
                        dateDebut.atStartOfDay(zone).toInstant()));
            }
            if (dateFin != null) {
                predicates.add(cb.lessThan(root.get("capturedAt"),
                        dateFin.plusDays(1).atStartOfDay(zone).toInstant()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
