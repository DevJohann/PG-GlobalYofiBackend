package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    // Values: TRANSFERENCIA, RECIBO_PAGO, RECOGER_TIENDA
    @Column(length = 50)
    private String metodo;

    // Values: PENDIENTE, VERIFICACION, VALIDADO
    @Column(length = 30)
    private String estado;

    // e.g. "PEDIDO-1234"
    @Column(length = 100)
    private String referencia;

    // URL path to the uploaded proof image
    @Column(name = "comprobante_url", columnDefinition = "TEXT")
    private String comprobanteUrl;

    @Builder.Default
    @Column(name = "fecha")
    private LocalDateTime fecha = LocalDateTime.now();
}
