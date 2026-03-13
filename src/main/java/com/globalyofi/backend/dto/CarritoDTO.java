package com.globalyofi.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {
    private Integer carritoId;
    private Integer clienteId;
    private List<ItemCarritoDTO> items;
    private BigDecimal totalEstimado;
}
