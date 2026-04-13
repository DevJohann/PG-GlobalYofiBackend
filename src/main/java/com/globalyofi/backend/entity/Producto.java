package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    private String marca;
    private Integer stockActual;
    private Integer stockMinimo;

    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;

    private String estado;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "tipo_piel")
    private String tipoPiel;

    private String acabado;
    private String tono;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
}
