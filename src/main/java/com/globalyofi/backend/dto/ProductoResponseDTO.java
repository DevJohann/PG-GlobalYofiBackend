package com.globalyofi.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoResponseDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String marca;
    private String imagenUrl;
    private String categoria;
    private String proveedor;
    private Integer categoriaId;
    private Integer proveedorId;
    private Integer stockActual;
    private Integer stockMinimo;
    private String estado;
}
