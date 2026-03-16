package com.globalyofi.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {
    private Integer idPedido;
    private Integer clienteId;
    private String nombreCliente;
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private String direccionEnvio;
    private String ciudadEnvio;
    private List<DetallePedidoDTO> detalles;
}
