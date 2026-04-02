package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.ProductoResponseDTO;
import com.globalyofi.backend.security.JwtAuthFilter;
import com.globalyofi.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de Integración para la capa Web (Controladores) de Productos.
 * Usamos @WebMvcTest para levantar únicamente el contenedor HTTP de Spring MVC.
 * Se deshabilitan los filtros mediante addFilters = false para probar puramente la lógica REST sin criptografía JWT.
 */
@WebMvcTest(controllers = ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Se mockea el Service porque aquí sólo nos interesa probar que el Controlador mapea bien el JSON y las rutas
    @MockitoBean
    private ProductoService productoService;

    // Mockeamos el filtro para que Spring Boot no falle al intentar levantar el contexto de seguridad
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void listarProductos_SinFiltros_RetornaHttp200YListaJSON() throws Exception {
        // --- Arrange ---
        ProductoResponseDTO dto1 = ProductoResponseDTO.builder()
                .id(1)
                .nombre("Labial Mate")
                .precio(new BigDecimal("15.50"))
                .build();

        ProductoResponseDTO dto2 = ProductoResponseDTO.builder()
                .id(2)
                .nombre("Base Cremosa")
                .precio(new BigDecimal("30.00"))
                .build();

        when(productoService.obtenerTodos()).thenReturn(List.of(dto1, dto2));

        // --- Act & Assert ---
        // mockMvc.perform() lanza una petición HTTP simulada, igual a la de Postman o Angular
        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos un 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Esperamos retorno Tipo JSON
                .andExpect(jsonPath("$.size()").value(2)) // Verificamos que el arreglo JSON tenga 2 productos
                .andExpect(jsonPath("$[0].nombre").value("Labial Mate")) // Comprobamos mapeo del primer elemento
                .andExpect(jsonPath("$[1].nombre").value("Base Cremosa"));
    }

    @Test
    void obtenerPorId_IdExistente_RetornaHttp200YObjetoJSON() throws Exception {
        // --- Arrange ---
        Integer productoId = 10;
        ProductoResponseDTO dtoSimulado = ProductoResponseDTO.builder()
                .id(productoId)
                .nombre("Serum Facial Gold")
                .precio(new BigDecimal("50.00"))
                .build();

        when(productoService.obtenerPorId(productoId)).thenReturn(dtoSimulado);

        // --- Act & Assert ---
        // Aquí pasamos el ID directamente a la URL
        mockMvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId))
                .andExpect(jsonPath("$.nombre").value("Serum Facial Gold"))
                .andExpect(jsonPath("$.precio").value(50.00));
    }
}
