package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Builder.Default
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(length = 50)
    private String estado;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "ciudad_envio", length = 100)
    private String ciudadEnvio;

    @Column(name = "direccion_envio", columnDefinition = "TEXT")
    private String direccionEnvio;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "telefono_pago", length = 20)
    private String telefonoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();
}
