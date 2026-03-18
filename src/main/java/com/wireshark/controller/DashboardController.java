package com.wireshark.controller;

import com.wireshark.dto.AddressCount;
import com.wireshark.dto.ProtocolCount;
import com.wireshark.dto.TimeBucketCount;
import com.wireshark.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Données pour les graphiques du tableau de bord")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/protocol-distribution")
    @Operation(summary = "Répartition par protocole", description = "Retourne le nombre de paquets par protocole (pour graphique camembert)")
    public ResponseEntity<List<ProtocolCount>> getProtocolDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(dashboardService.getProtocolDistribution(dateDebut, dateFin));
    }

    @GetMapping("/packets-by-date")
    @Operation(summary = "Paquets par date", description = "Retourne le nombre de paquets par date (pour graphique en ligne)")
    public ResponseEntity<List<TimeBucketCount>> getPacketsOverTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(dashboardService.getPacketsOverTime(dateDebut, dateFin));
    }

    @GetMapping("/top5-sources")
    @Operation(summary = "Top 5 sources", description = "Retourne les 5 sources (IP:port) les plus actives")
    public ResponseEntity<List<AddressCount>> getTop5Sources(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(dashboardService.getTop5Sources(dateDebut, dateFin));
    }

    @GetMapping("/top5-destinations")
    @Operation(summary = "Top 5 destinations", description = "Retourne les 5 destinations (IP:port) les plus actives")
    public ResponseEntity<List<AddressCount>> getTop5Destinations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(dashboardService.getTop5Destinations(dateDebut, dateFin));
    }
}
