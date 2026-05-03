package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.ConfiguracionDTO;
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
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final PagoConfigRepository pagoConfigRepository;

    @org.springframework.beans.factory.annotation.Value("${uploads.dir:uploads}")
    private String uploadsDir;

    // ──────────────────────────────────────────────────────────────
    // CONFIG ENDPOINTS
    // ──────────────────────────────────────────────────────────────

    /** Devuelve la configuración completa del sistema (acceso público) */
    @GetMapping("/config")
    public ConfiguracionDTO obtenerConfiguracion() {
        PagoConfig config = pagoConfigRepository.findById(1L)
                .orElse(defaultConfig());
        return toDTO(config);
    }

    /** Actualiza la configuración (solo ADMIN) */
    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionDTO actualizarConfiguracion(@RequestBody ConfiguracionDTO req) {
        PagoConfig config = pagoConfigRepository.findById(1L).orElse(new PagoConfig());
        config.setId(1L);

        // Datos de transferencia / QR
        if (req.getNequiNumero() != null)
            config.setNequiNumero(req.getNequiNumero());
        if (req.getNequiNombre() != null)
            config.setNequiNombre(req.getNequiNombre());
        if (req.getQrTexto() != null)
            config.setQrTexto(req.getQrTexto());
        if (req.getQrInfoLabel1() != null)
            config.setQrInfoLabel1(req.getQrInfoLabel1());
        if (req.getQrInfoValue1() != null)
            config.setQrInfoValue1(req.getQrInfoValue1());
        if (req.getQrInfoLabel2() != null)
            config.setQrInfoLabel2(req.getQrInfoLabel2());
        if (req.getQrInfoValue2() != null)
            config.setQrInfoValue2(req.getQrInfoValue2());

        // Condiciones de compra y precios
        if (req.getCondicionesCompra() != null)
            config.setCondicionesCompra(req.getCondicionesCompra());
        if (req.getPrecioEnvioGratis() != null)
            config.setPrecioEnvioGratis(req.getPrecioEnvioGratis());
        if (req.getPrecioEnvio() != null)
            config.setPrecioEnvio(req.getPrecioEnvio());

        // Contacto
        if (req.getWhatsappNumero() != null)
            config.setWhatsappNumero(req.getWhatsappNumero());
        if (req.getContactoTelefono() != null)
            config.setContactoTelefono(req.getContactoTelefono());
        if (req.getContactoEmail() != null)
            config.setContactoEmail(req.getContactoEmail());

        // Tienda
        if (req.getTiendaDireccion() != null)
            config.setTiendaDireccion(req.getTiendaDireccion());
        if (req.getTiendaHorario() != null)
            config.setTiendaHorario(req.getTiendaHorario());
        if (req.getTiendaTiempoPreparacion() != null)
            config.setTiendaTiempoPreparacion(req.getTiendaTiempoPreparacion());

        // Habilitación de medios de pago
        if (req.getHabilitarTransferencia() != null)
            config.setHabilitarTransferencia(req.getHabilitarTransferencia());
        if (req.getHabilitarReciboPago() != null)
            config.setHabilitarReciboPago(req.getHabilitarReciboPago());
        if (req.getHabilitarRecogerTienda() != null)
            config.setHabilitarRecogerTienda(req.getHabilitarRecogerTienda());

        return toDTO(pagoConfigRepository.save(config));
    }

    /** Sube imagen del QR (solo ADMIN) */
    @PostMapping(value = "/config/qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionDTO subirQr(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadsDir, "qr");
        java.nio.file.Files.createDirectories(uploadPath);
        java.nio.file.Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        String fileUrl = "/uploads/qr/" + filename;
        PagoConfig config = pagoConfigRepository.findById(1L).orElse(new PagoConfig());
        config.setId(1L);
        config.setQrImageUrl(fileUrl);
        return toDTO(pagoConfigRepository.save(config));
    }

    // ──────────────────────────────────────────────────────────────
    // PAYMENT FLOW ENDPOINTS
    // ──────────────────────────────────────────────────────────────

    @PostMapping("/{pedidoId}/iniciar")
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO iniciarPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.iniciarPago(pedidoId);
    }

    @PostMapping(value = "/{pedidoId}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO subirComprobante(
            @PathVariable("pedidoId") Integer pedidoId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return pagoService.subirComprobante(pedidoId, file);
    }

    @PatchMapping("/{pedidoId}/validar")
    @PreAuthorize("hasRole('ADMIN')")
    public PagoResponseDTO validarPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.validarPago(pedidoId);
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public PagoResponseDTO obtenerPago(@PathVariable("pedidoId") Integer pedidoId) {
        return pagoService.obtenerPago(pedidoId);
    }

    // ──────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────

    private PagoConfig defaultConfig() {
        return PagoConfig.builder()
                .id(1L)
                .nequiNumero("No configurado")
                .nequiNombre("Global Yofi")
                .qrTexto("Nequi")
                .condicionesCompra("🚚 Envío gratis en compras superiores a $150.000 · ✨ 100% Autenticidad garantizada")
                .precioEnvioGratis(new BigDecimal("150000"))
                .precioEnvio(new BigDecimal("15000"))
                .tiendaDireccion("Calle 6 # 2-26, Gama, Cundinamarca")
                .tiendaHorario("Lunes a Sábado: 8am – 6pm")
                .tiendaTiempoPreparacion("1-2 días hábiles")
                .habilitarTransferencia(true)
                .habilitarReciboPago(true)
                .habilitarRecogerTienda(true)
                .build();
    }

    private ConfiguracionDTO toDTO(PagoConfig c) {
        return ConfiguracionDTO.builder()
                .nequiNumero(c.getNequiNumero())
                .nequiNombre(c.getNequiNombre())
                .qrImageUrl(c.getQrImageUrl())
                .qrTexto(c.getQrTexto() != null ? c.getQrTexto() : "Nequi")
                .qrInfoLabel1(c.getQrInfoLabel1())
                .qrInfoValue1(c.getQrInfoValue1())
                .qrInfoLabel2(c.getQrInfoLabel2())
                .qrInfoValue2(c.getQrInfoValue2())
                .condicionesCompra(c.getCondicionesCompra())
                .precioEnvioGratis(
                        c.getPrecioEnvioGratis() != null ? c.getPrecioEnvioGratis() : new BigDecimal("150000"))
                .precioEnvio(c.getPrecioEnvio() != null ? c.getPrecioEnvio() : new BigDecimal("15000"))
                .whatsappNumero(c.getWhatsappNumero())
                .contactoTelefono(c.getContactoTelefono())
                .contactoEmail(c.getContactoEmail())
                .tiendaDireccion(
                        c.getTiendaDireccion() != null ? c.getTiendaDireccion() : "Calle 6 # 2-26, Gama, Cundinamarca")
                .tiendaHorario(c.getTiendaHorario() != null ? c.getTiendaHorario() : "Lunes a Sábado: 8am – 6pm")
                .tiendaTiempoPreparacion(
                        c.getTiendaTiempoPreparacion() != null ? c.getTiendaTiempoPreparacion() : "1-2 días hábiles")
                .habilitarTransferencia(c.getHabilitarTransferencia() != null ? c.getHabilitarTransferencia() : true)
                .habilitarReciboPago(c.getHabilitarReciboPago() != null ? c.getHabilitarReciboPago() : true)
                .habilitarRecogerTienda(c.getHabilitarRecogerTienda() != null ? c.getHabilitarRecogerTienda() : true)
                .build();
    }
}
