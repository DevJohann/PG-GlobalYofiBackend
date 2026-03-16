package com.globalyofi.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {
    private Integer clienteId;
    private String metodoPago;
    private String direccion;
    private String ciudad;
}
