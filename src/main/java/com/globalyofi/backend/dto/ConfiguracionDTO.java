package com.globalyofi.backend.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO para la configuración dinámica del sistema.
 * Se usa en GET /api/pagos/config y POST /api/pagos/config.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionDTO {

    // ── Datos de transferencia / QR ───────────────────────────────
    private String nequiNumero;
    private String nequiNombre;
    private String qrImageUrl;
    private String qrTexto;
    private String qrInfoLabel1;
    private String qrInfoValue1;
    private String qrInfoLabel2;
    private String qrInfoValue2;

    // ── Condiciones de compra ─────────────────────────────────────
    private String condicionesCompra;

    // ── Precios de envío ──────────────────────────────────────────
    private BigDecimal precioEnvioGratis;
    private BigDecimal precioEnvio;

    // ── Datos de contacto ─────────────────────────────────────────
    private String whatsappNumero;
    private String contactoTelefono;
    private String contactoEmail;

    // ── Datos de tienda física ────────────────────────────────────
    private String tiendaDireccion;
    private String tiendaHorario;
    private String tiendaTiempoPreparacion;

    // ── Habilitación de medios de pago ────────────────────────────
    private Boolean habilitarTransferencia;
    private Boolean habilitarReciboPago;
    private Boolean habilitarRecogerTienda;
}
