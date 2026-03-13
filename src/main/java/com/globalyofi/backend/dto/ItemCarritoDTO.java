package com.globalyofi.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {
    private Integer itemId;
    private Integer productoId;
    private String nombreProducto;
    private BigDecimal precio;
    private Integer cantidad;
    private BigDecimal subtotal;
}
