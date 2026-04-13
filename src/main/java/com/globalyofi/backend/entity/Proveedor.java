package com.globalyofi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    private String nombre;
    private String contactoPrincipal;
    private String telefono;
    private String email;
    private String direccion;
    private String ciudad;
    private String nit;
    private String estado;
    private boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}
