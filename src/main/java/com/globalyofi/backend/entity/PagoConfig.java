package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoConfig {

    @Id
    private Long id; // Always 1 (singleton)

    // ── Datos de pago por transferencia ──────────────────────────
    @Column(name = "nequi_numero")
    private String nequiNumero;

    @Column(name = "nequi_nombre")
    private String nequiNombre;

    @Column(name = "qr_image_url")
    private String qrImageUrl;

    /** Texto que aparece encima/al lado del QR (ej: "Nequi", "Daviplata", etc.) */
    @Column(name = "qr_texto", length = 100)
    private String qrTexto;

    /** Label del primer cuadro informativo al lado derecho del QR */
    @Column(name = "qr_info_label1", length = 200)
    private String qrInfoLabel1;

    /** Valor del primer cuadro informativo al lado derecho del QR */
    @Column(name = "qr_info_value1", length = 200)
    private String qrInfoValue1;

    /** Label del segundo cuadro informativo al lado derecho del QR */
    @Column(name = "qr_info_label2", length = 200)
    private String qrInfoLabel2;

    /** Valor del segundo cuadro informativo al lado derecho del QR */
    @Column(name = "qr_info_value2", length = 200)
    private String qrInfoValue2;

    // ── Condiciones de compra (detalle de producto) ───────────────
    @Column(name = "condiciones_compra", columnDefinition = "TEXT")
    private String condicionesCompra;

    // ── Precios de envío ──────────────────────────────────────────
    @Column(name = "precio_envio_gratis", precision = 15, scale = 2)
    private BigDecimal precioEnvioGratis;

    @Column(name = "precio_envio", precision = 15, scale = 2)
    private BigDecimal precioEnvio;

    // ── Datos de contacto globales ────────────────────────────────
    @Column(name = "whatsapp_numero", length = 20)
    private String whatsappNumero;

    @Column(name = "contacto_telefono", length = 30)
    private String contactoTelefono;

    @Column(name = "contacto_email", length = 150)
    private String contactoEmail;

    // ── Datos de tienda física ────────────────────────────────────
    @Column(name = "tienda_direccion", length = 255)
    private String tiendaDireccion;

    @Column(name = "tienda_horario", length = 100)
    private String tiendaHorario;

    @Column(name = "tienda_tiempo_preparacion", length = 100)
    private String tiendaTiempoPreparacion;

    // ── Habilitación de medios de pago ────────────────────────────
    @Builder.Default
    @Column(name = "habilitar_transferencia")
    private Boolean habilitarTransferencia = true;

    @Builder.Default
    @Column(name = "habilitar_recibo_pago")
    private Boolean habilitarReciboPago = true;

    @Builder.Default
    @Column(name = "habilitar_recoger_tienda")
    private Boolean habilitarRecogerTienda = true;
}
