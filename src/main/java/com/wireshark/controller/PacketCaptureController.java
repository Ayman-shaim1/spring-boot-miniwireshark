package com.wireshark.controller;

import com.wireshark.service.PacketCaptureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/capture")
@Tag(name = "Capture", description = "Gestion de la capture de paquets réseau")
public class PacketCaptureController {

    private final PacketCaptureService captureService;

    public PacketCaptureController(PacketCaptureService captureService) {
        this.captureService = captureService;
    }

    @GetMapping("/interfaces")
    @Operation(summary = "Lister les interfaces", description = "Retourne la liste des interfaces réseau disponibles")
    public ResponseEntity<List<String>> listInterfaces() throws PcapNativeException {
        return ResponseEntity.ok(captureService.listInterfaces());
    }

    @PostMapping("/start")
    @Operation(summary = "Démarrer la capture", description = "Démarre la capture sur l'interface spécifiée")
    public ResponseEntity<?> start(@RequestBody Map<String, String> body) {
        String interfaceName = body.get("interface");
        if (interfaceName == null || interfaceName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "interface requis"));
        }
        try {
            if (captureService.isRunning()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Capture déjà en cours"));
            }
            captureService.start(interfaceName);
            return ResponseEntity.ok(Map.of("message", "Capture démarrée sur " + interfaceName));
        } catch (PcapNativeException | NotOpenException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    @Operation(summary = "Arrêter la capture")
    public ResponseEntity<Map<String, String>> stop() {
        captureService.stop();
        return ResponseEntity.ok(Map.of("message", "Capture arrêtée"));
    }

    @GetMapping("/status")
    @Operation(summary = "Statut", description = "Indique si la capture est en cours")
    public ResponseEntity<Map<String, Boolean>> status() {
        return ResponseEntity.ok(Map.of("running", captureService.isRunning()));
    }
}
