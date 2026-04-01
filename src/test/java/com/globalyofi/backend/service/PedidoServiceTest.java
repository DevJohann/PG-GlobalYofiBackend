package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.PedidoResponseDTO;
import com.globalyofi.backend.entity.*;
import com.globalyofi.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    // --- Mocks para todas las dependencias del PedidoService ---
    @Mock private PedidoRepository pedidoRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private PagoRepository pagoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void obtenerPorId_IdExistente_RetornaPedidoDTO() {
        // --- Arrange ---
        Integer pedidoId = 100;
        
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan Lopez");
        usuario.setEmail("juan@test.com");
        
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setUsuario(usuario);
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("12345");

        Pedido pedidoSimulado = Pedido.builder()
                .idPedido(pedidoId)
                .cliente(cliente)
                .fechaPedido(LocalDateTime.now())
                .total(new BigDecimal("150.00"))
                .estado(PedidoService.ESTADO_PENDIENTE)
                .metodoPago("TARJETA")
                .detalles(new ArrayList<>())
                .build();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedidoSimulado));

        // --- Act ---
        PedidoResponseDTO resultado = pedidoService.obtenerPorId(pedidoId);

        // --- Assert ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(pedidoId);
        assertThat(resultado.getNombreCliente()).isEqualTo("Juan Lopez");
        assertThat(resultado.getTotal()).isEqualTo(new BigDecimal("150.00"));
        assertThat(resultado.getEstado()).isEqualTo(PedidoService.ESTADO_PENDIENTE);

        verify(pedidoRepository, times(1)).findById(pedidoId);
    }

    @Test
    void realizarPedido_CarritoVacio_LanzaRuntimeException() {
        // --- Arrange ---
        com.globalyofi.backend.dto.PedidoRequestDTO request = new com.globalyofi.backend.dto.PedidoRequestDTO();
        request.setClienteId(1);

        Carrito carritoExistenteVacio = new Carrito();
        carritoExistenteVacio.setEstado("activo");
        carritoExistenteVacio.setItems(new ArrayList<>()); // Carrito sin ítems

        when(carritoRepository.findByClienteIdAndEstado(1, "activo"))
                .thenReturn(Optional.of(carritoExistenteVacio));

        // --- Act & Assert ---
        assertThatThrownBy(() -> pedidoService.realizarPedido(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El carrito está vacío");

        // Verificamos que jamás se intentó guardar un pedido corrupto si el carrito estaba vacío
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void actualizarEstado_ACancelado_RestauraInventario() {
        // --- Arrange ---
        Integer pedidoId = 50;

        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@test.com");
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);

        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombre("Crema Humectante");
        producto.setStockActual(10); // Tiene 10 actualmente

        DetallePedido detalle = DetallePedido.builder()
                .producto(producto)
                .cantidad(2) // Se compraron 2 (el stock antes era 12)
                .precioUnitario(new BigDecimal("20.00"))
                .build();

        Pedido pedidoSimulado = Pedido.builder()
                .idPedido(pedidoId)
                .cliente(cliente)
                .estado(PedidoService.ESTADO_PENDIENTE)
                .detalles(List.of(detalle))
                .build();

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedidoSimulado));
        
        // Simulamos que al hacer save() se devuelve la entidad misma
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- Act ---
        // Cambiamos el estado a Cancelado para disparar la restauración de stock
        PedidoResponseDTO resultado = pedidoService.actualizarEstado(pedidoId, PedidoService.ESTADO_CANCELADO);

        // --- Assert ---
        assertThat(resultado.getEstado()).isEqualTo(PedidoService.ESTADO_CANCELADO);

        // Verificamos matemáticamente que el stock original 10 se le sumó la cantidad cancelada 2 -> stock final 12
        assertThat(producto.getStockActual()).isEqualTo(12);

        // Verificamos y obligamos a que el sistema HAYAN hecho uso del save de la Base de Datos para guardar este nuevo valor!
        verify(productoRepository, times(1)).save(producto);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
        verify(pedidoRepository, times(1)).save(pedidoSimulado);
    }
}
