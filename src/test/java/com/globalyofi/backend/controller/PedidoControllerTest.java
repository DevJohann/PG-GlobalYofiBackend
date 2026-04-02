package com.globalyofi.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalyofi.backend.dto.PedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoResponseDTO;
import com.globalyofi.backend.security.JwtAuthFilter;
import com.globalyofi.backend.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Creador maestro de JSON de Spring

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void listarPedidos_RetornaHttp200YListaDePedidos() throws Exception {
        // --- Arrange ---
        PedidoResponseDTO pedidoDto = PedidoResponseDTO.builder()
                .id(101)
                .nombreCliente("Ana Gomez")
                .estado("Pagado")
                .total(new BigDecimal("99.99"))
                .fechaPedido(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(pedidoService.obtenerTodos()).thenReturn(List.of(pedidoDto));

        // --- Act & Assert ---
        mockMvc.perform(get("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].nombreCliente").value("Ana Gomez"))
                .andExpect(jsonPath("$[0].estado").value("Pagado"));
    }

    @Test
    void realizarPedido_JsonValido_RetornaHttp200YPedidoDTO() throws Exception {
        // --- Arrange ---
        // 1. Configuramos lo que el CLiente (Angular) enviaría en el Body del POST
        PedidoRequestDTO requestCliente = new PedidoRequestDTO();
        requestCliente.setClienteId(5);
        requestCliente.setMetodoPago("EFECTIVO");
        requestCliente.setDireccion("Calle 123");
        requestCliente.setCiudad("Bogotá");
        requestCliente.setTipoDocumento("CC");
        requestCliente.setNumeroDocumento("102030");

        // 2. Configuramos lo que el servicio respondería después de procesar todo exitosamente
        PedidoResponseDTO responseBackend = PedidoResponseDTO.builder()
                .id(505)
                .nombreCliente("Cliente Local")
                .estado(PedidoService.ESTADO_PENDIENTE_PAGO)
                .metodoPago("EFECTIVO")
                .build();

        when(pedidoService.realizarPedido(any(PedidoRequestDTO.class))).thenReturn(responseBackend);

        // --- Act & Assert ---
        // Aquí pasamos de objeto Java a cadena pura JSON usando Jackson ObjectMapper
        String jsonPayload = objectMapper.writeValueAsString(requestCliente);

        mockMvc.perform(post("/api/pedidos/realizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload)) // Inyectamos el JSON String simulando la red
                .andExpect(status().isOk()) // Esperamos éxito en la recepción
                .andExpect(jsonPath("$.id").value(505))
                .andExpect(jsonPath("$.estado").value(PedidoService.ESTADO_PENDIENTE_PAGO));
    }
}
