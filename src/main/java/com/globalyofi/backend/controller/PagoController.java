package com.globalyofi.backend.controller;

import com.globalyofi.backend.entity.PagoConfig;
import com.globalyofi.backend.repository.PagoConfigRepository;
import com.globalyofi.backend.dto.PagoResponseDTO;
import com.globalyofi.backend.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final PagoConfigRepository pagoConfigRepository;

    @org.springframework.beans.factory.annotation.Value("${uploads.dir:uploads}")
    private String uploadsDir;

    // --- CONFIG ENABLEMENT FOR ADMIN ---
    
    @GetMapping("/config")
    public PagoConfig obtenerConfiguracion() {
        return pagoConfigRepository.findById(1L)
                .orElse(PagoConfig.builder()
                        .id(1L)
                        .nequiNumero("No configurado")
                        .nequiNombre("No configurado")
                        .qrImageUrl(null)
                        .build());
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public PagoConfig actualizarConfiguracion(@RequestBody PagoConfig configReq) {
        PagoConfig config = pagoConfigRepository.findById(1L).orElse(new PagoConfig());
        config.setId(1L); // Force ID 1
        config.setNequiNumero(configReq.getNequiNumero());
        config.setNequiNombre(configReq.getNequiNombre());
        if (configReq.getQrImageUrl() != null && !configReq.getQrImageUrl().isEmpty()) {
            config.setQrImageUrl(configReq.getQrImageUrl());
        }
        return pagoConfigRepository.save(config);
    }
    
    @PostMapping(value = "/config/qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public PagoConfig subirQr(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadsDir, "qr");
        java.nio.file.Files.createDirectories(uploadPath);
        java.nio.file.Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        String fileUrl = "/uploads/qr/" + filename;
        PagoConfig config = pagoConfigRepository.findById(1L).orElse(new PagoConfig());
        config.setId(1L);
        config.setQrImageUrl(fileUrl);
        return pagoConfigRepository.save(config);
    }
    
    // --- PAYMENT FLOW ---

    // Called by frontend after order creation to initialize the payment record (idempotent)
    @PostMapping("/{pedidoId}/iniciar")
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO iniciarPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.iniciarPago(pedidoId);
    }

    // Customer uploads payment proof image
    @PostMapping(value = "/{pedidoId}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO subirComprobante(
            @PathVariable("pedidoId") Integer pedidoId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return pagoService.subirComprobante(pedidoId, file);
    }

    // Admin validates the payment
    @PatchMapping("/{pedidoId}/validar")
    @PreAuthorize("hasRole('ADMIN')")
    public PagoResponseDTO validarPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.validarPago(pedidoId);
    }

    // Get payment info for a given order (customer or admin)
    @GetMapping("/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO obtenerPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.obtenerPago(pedidoId);
    }
}
