package com.globalyofi.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {
    private Integer id;
    private Integer clienteId;
    private String nombreCliente;
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private String direccion;
    private String ciudad;
    private List<DetallePedidoDTO> items;

    // Métodos para compatibilidad con el frontend anterior
    @JsonProperty("idPedido")
    public Integer getIdPedido() {
        return id;
    }

    @JsonProperty("direccionEnvio")
    public String getDireccionEnvio() {
        return direccion;
    }

    @JsonProperty("ciudadEnvio")
    public String getCiudadEnvio() {
        return ciudad;
    }

    @JsonProperty("detalles")
    public List<DetallePedidoDTO> getDetalles() {
        return items;
    }
}
