package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.DetallePedidoDTO;
import com.globalyofi.backend.dto.PedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoResponseDTO;
import com.globalyofi.backend.entity.*;
import com.globalyofi.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    // Constantes para estados de pedido (Estilo Amazon)
    public static final String ESTADO_PENDIENTE = "Pendiente de envío";
    public static final String ESTADO_PREPARANDO = "Preparando envío";
    public static final String ESTADO_ENVIADO = "Enviado";
    public static final String ESTADO_EN_REPARTO = "En reparto";
    public static final String ESTADO_ENTREGADO = "Entregado";

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    @Transactional
    public PedidoResponseDTO realizarPedido(PedidoRequestDTO request) {
        Integer clienteId = request.getClienteId();
        String metodoPago = request.getMetodoPago();
        String direccion = request.getDireccion();
        String ciudad = request.getCiudad();

        // 1. Obtener carrito activo
        Carrito carrito = carritoRepository.findByClienteIdAndEstado(clienteId, "activo")
                .orElseThrow(() -> new RuntimeException("No hay un carrito activo para este cliente"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Cliente cliente = carrito.getCliente();

        // Actualizar perfil si se proporcionan datos
        if (direccion != null && !direccion.isEmpty()) {
            cliente.setDireccion(direccion);
        }
        if (ciudad != null && !ciudad.isEmpty()) {
            cliente.setCiudad(ciudad);
        }
        if (request.getTipoDocumento() != null && !request.getTipoDocumento().isEmpty()) {
            cliente.setTipoDocumento(request.getTipoDocumento());
        }
        if (request.getNumeroDocumento() != null && !request.getNumeroDocumento().isEmpty()) {
            cliente.setNumeroDocumento(request.getNumeroDocumento());
        }

        if (cliente.getDireccion() == null || cliente.getDireccion().isEmpty() ||
            cliente.getCiudad() == null || cliente.getCiudad().isEmpty() ||
            cliente.getTipoDocumento() == null || cliente.getTipoDocumento().isEmpty() ||
            cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().isEmpty()) {
            throw new RuntimeException("Por favor, verifica que tu perfil de cliente esté completo (dirección, ciudad y documento) antes de realizar el pedido.");
        }

        // 2. Validar Stock y preparar detalles
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();
            if (producto.getStockActual() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
        }

        // 3. Crear el Pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .fechaPedido(LocalDateTime.now())
                .total(carrito.getTotalEstimado())
                .estado(ESTADO_PENDIENTE)
                .metodoPago(metodoPago)
                .ciudadEnvio(ciudad != null ? ciudad : cliente.getCiudad())
                .direccionEnvio(direccion != null ? direccion : cliente.getDireccion())
                .observaciones(request.getObservaciones())
                .build();

        // 4. Crear detalles, restar stock y registrar movimientos
        List<DetallePedido> detalles = carrito.getItems().stream().map(item -> {
            Producto producto = item.getProducto();
            int stockAnterior = producto.getStockActual();
            int stockNuevo = stockAnterior - item.getCantidad();
            
            // Restar stock
            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);

            // Registrar movimiento en Inventario
            Inventario movimiento = Inventario.builder()
                    .producto(producto)
                    .usuario(cliente.getUsuario())
                    .tipoMovimiento("salida")
                    .cantidad(item.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(stockNuevo)
                    .fechaMovimiento(LocalDateTime.now())
                    .observaciones("Venta - Pedido en proceso")
                    .build();
            inventarioRepository.save(movimiento);

            return DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                    .build();
        }).collect(Collectors.toList());

        pedido.setDetalles(detalles);

        // 5. Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 6. Vaciar carrito
        carrito.getItems().clear();
        carrito.setTotalEstimado(BigDecimal.ZERO);
        carritoRepository.save(carrito);

        return mapToDTO(pedidoGuardado);
    }

    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoRepository.findAll().stream()
                .sorted((p1, p2) -> p2.getFechaPedido().compareTo(p1.getFechaPedido()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> obtenerMisPedidos(String email) {
        return pedidoRepository.findByClienteUsuarioEmailOrderByFechaPedidoDesc(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO obtenerPorId(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        return mapToDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO actualizarEstado(Integer id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        pedido.setEstado(nuevoEstado);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return mapToDTO(pedidoGuardado);
    }

    private PedidoResponseDTO mapToDTO(Pedido pedido) {
        List<DetallePedidoDTO> detallesDTO = pedido.getDetalles().stream()
                .map(d -> DetallePedidoDTO.builder()
                        .idDetalle(d.getIdDetalle())
                        .productoId(d.getProducto().getIdProducto())
                        .nombreProducto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponseDTO.builder()
                .id(pedido.getIdPedido())
                .clienteId(pedido.getCliente().getIdCliente())
                .nombreCliente(pedido.getCliente().getUsuario().getNombre())
                .emailCliente(pedido.getCliente().getUsuario().getEmail())
                .fechaPedido(pedido.getFechaPedido())
                .total(pedido.getTotal())
                .estado(pedido.getEstado())
                .metodoPago(pedido.getMetodoPago())
                .direccion(pedido.getDireccionEnvio())
                .ciudad(pedido.getCiudadEnvio())
                .tipoDocumento(pedido.getCliente().getTipoDocumento())
                .numeroDocumento(pedido.getCliente().getNumeroDocumento())
                .observaciones(pedido.getObservaciones())
                .items(detallesDTO)
                .build();
    }
}
