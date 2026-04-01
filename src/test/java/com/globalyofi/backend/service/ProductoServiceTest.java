package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.ProductoResponseDTO;
import com.globalyofi.backend.entity.Categoria;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.entity.Proveedor;
import com.globalyofi.backend.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Clase de prueba unitaria para {@link ProductoService}.
 * <p>
 * Se utiliza BDD (Behavior Driven Development) para nombrar los métodos:
 *  Metodo_EstadoOInput_ResultadoEsperado
 * <p>
 * Implementa @ExtendWith(MockitoExtension.class) para aislar la capa de servicio
 * y no cargar el contexto completo de Spring Boot, haciendo los tests veloces.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    // @Mock crea un "doble", es decir, un objeto falso configurado.
    @Mock
    private ProductoRepository productoRepository;

    // @InjectMocks toma los @Mock declarados y los inserta en el servicio real.
    // Esto asegura que probemos el "Servicio" pero usando el "Repositorio" de mentiras.
    @InjectMocks
    private ProductoService productoService;

    @Test
    void obtenerPorId_IdExistente_RetornaProductoDTO() {
        // --- Arrange (Preparar) ---
        // 1. Configuramos nuestros datos de prueba
        Integer productoId = 1;

        Categoria categoria = new Categoria();
        categoria.setIdCategoria(10);
        categoria.setNombre("Bases");

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(20);
        proveedor.setNombre("L'Oreal");

        Producto productoFalso = new Producto();
        productoFalso.setIdProducto(productoId);
        productoFalso.setNombre("Base Líquida Fit Me");
        productoFalso.setPrecio(new BigDecimal("35.50"));
        productoFalso.setCategoria(categoria);
        productoFalso.setProveedor(proveedor);

        // 2. Le decimos al Mock qué hacer cuando el servicio llame a 'findById'
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoFalso));

        // --- Act (Actuar) ---
        // Llamamos al método real que queremos probar
        ProductoResponseDTO resultado = productoService.obtenerPorId(productoId);

        // --- Assert (Afirmar) ---
        // Verificamos que el resultado no es nulo y coincida con las expectativas
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(productoId);
        assertThat(resultado.getNombre()).isEqualTo("Base Líquida Fit Me");
        assertThat(resultado.getCategoria()).isEqualTo("Bases");
        assertThat(resultado.getProveedor()).isEqualTo("L'Oreal");

        // Verificamos que el repositorio efectivamente fue llamado una sola vez
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void obtenerPorId_IdNoExistente_LanzaEntityNotFoundException() {
        // --- Arrange ---
        Integer productoId = 999;
        
        // Simulamos que la BD devuelve "vacío"
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        // AssertJ nos permite verificar fácilmente las Excepciones
        assertThatThrownBy(() -> productoService.obtenerPorId(productoId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Producto no encontrado con el id: 999");
        
        // Comprobamos que sí se buscó
        verify(productoRepository, times(1)).findById(productoId);
    }

    @Test
    void obtenerTodos_ExistenProductos_RetornaListaDeDTOs() {
        // --- Arrange ---
        Producto p1 = new Producto();
        p1.setIdProducto(1);
        p1.setNombre("Labial Rojo");

        Producto p2 = new Producto();
        p2.setIdProducto(2);
        p2.setNombre("Delineador Negro");

        List<Producto> listaSimulada = List.of(p1, p2);
        when(productoRepository.findAll()).thenReturn(listaSimulada);

        // --- Act ---
        List<ProductoResponseDTO> resultados = productoService.obtenerTodos();

        // --- Assert ---
        assertThat(resultados).isNotNull();
        assertThat(resultados.size()).isEqualTo(2);
        assertThat(resultados.get(0).getNombre()).isEqualTo("Labial Rojo");
        assertThat(resultados.get(1).getNombre()).isEqualTo("Delineador Negro");

        verify(productoRepository, times(1)).findAll();
    }
}
