package com.globalyofi.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {
    private Long idPago;
    private Integer pedidoId;
    private String metodo;
    private String estado;
    private String referencia;
    private String comprobanteUrl;
    private LocalDateTime fecha;

    // Payment context
    private BigDecimal totalPedido;
    private String estadoPedido;
    private String nombreCliente;
    private String emailCliente;
}
