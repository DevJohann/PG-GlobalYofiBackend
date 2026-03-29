package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pago_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoConfig {
    @Id
    private Long id; // Always 1
    
    @Column(name = "nequi_numero")
    private String nequiNumero;
    
    @Column(name = "nequi_nombre")
    private String nequiNombre;
    
    @Column(name = "qr_image_url")
    private String qrImageUrl;
}
