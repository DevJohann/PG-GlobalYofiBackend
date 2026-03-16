package com.globalyofi.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_reporte", length = 100)
    private String tipoReporte;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(columnDefinition = "VARCHAR(500)")
    private String parametros;

    @Column(name = "archivo_resultado", length = 255)
    private String archivoResultado;

    @Column(length = 50)
    private String estado;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
}
