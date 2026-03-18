package com.wireshark.controller;

import com.wireshark.entity.PacketHistory;
import com.wireshark.repository.PacketHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@Tag(name = "Historique", description = "Historique des paquets capturés")
public class PacketHistoryController {

    private final PacketHistoryRepository packetHistoryRepository;

    public PacketHistoryController(PacketHistoryRepository packetHistoryRepository) {
        this.packetHistoryRepository = packetHistoryRepository;
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
        Specification<PacketHistory> spec = buildSpecification(source, destination, protocol, length, info, dateDebut, dateFin);
        var result = packetHistoryRepository.findAll(spec, pageable);
        return ResponseEntity.ok(result.getContent());
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
