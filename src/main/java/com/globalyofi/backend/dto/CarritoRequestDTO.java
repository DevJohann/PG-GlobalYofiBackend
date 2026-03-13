package com.globalyofi.backend.dto;

import lombok.Data;

@Data
public class CarritoRequestDTO {
    private Integer clienteId;
    private Integer productoId;
    private Integer cantidad;
}
