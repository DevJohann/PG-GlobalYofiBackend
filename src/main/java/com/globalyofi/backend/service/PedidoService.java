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

    // Constantes para estados de pago
    public static final String ESTADO_PENDIENTE_PAGO = "Pendiente de Pago";
    public static final String ESTADO_PENDIENTE_VERIFICACION = "Pendiente Verificación Pago";
    public static final String ESTADO_PAGADO = "Pagado";
    public static final String ESTADO_CANCELADO = "Cancelado";

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final PagoRepository pagoRepository;
    private final PagoConfigRepository pagoConfigRepository;

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

        // 2b. Calcular costo de envío
        BigDecimal costoEnvio = BigDecimal.ZERO;
        if (!"RECOGER_TIENDA".equals(metodoPago) && ! "RECOGER_TIENDA".equals(direccion)) {
            PagoConfig config = pagoConfigRepository.findById(1L).orElse(null);
            if (config != null) {
                BigDecimal subtotal = carrito.getTotalEstimado();
                BigDecimal umbralGratis = config.getPrecioEnvioGratis() != null ? config.getPrecioEnvioGratis() : BigDecimal.valueOf(150000);
                if (subtotal.compareTo(umbralGratis) < 0) {
                    costoEnvio = config.getPrecioEnvio() != null ? config.getPrecioEnvio() : BigDecimal.valueOf(15000);
                }
            }
        }
        
        BigDecimal totalFInal = carrito.getTotalEstimado().add(costoEnvio);

        // 3. Crear el Pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .fechaPedido(LocalDateTime.now())
                .total(totalFInal)
                .estado(ESTADO_PENDIENTE_PAGO)
                .metodoPago(metodoPago)
                .ciudadEnvio(ciudad != null ? ciudad : cliente.getCiudad())
                .direccionEnvio(direccion != null ? direccion : cliente.getDireccion())
                .observaciones(request.getObservaciones())
                .telefonoPago(request.getTelefonoPago())
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

        // 5a. Crear registro de Pago automáticamente
        Pago pago = Pago.builder()
                .pedido(pedidoGuardado)
                .metodo(metodoPago)
                .estado("PENDIENTE")
                .referencia("PEDIDO-" + pedidoGuardado.getIdPedido())
                .build();
        pagoRepository.save(pago);

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

    public boolean esDuenioDelPedido(Integer idPedido, String email) {
        return pedidoRepository.findById(idPedido)
                .map(p -> p.getCliente().getUsuario().getEmail().equals(email))
                .orElse(false);
    }

    @Transactional
    public PedidoResponseDTO actualizarEstado(Integer id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        String estadoAnterior = pedido.getEstado();
        
        // Si el pedido se cancela, restaurar stock
        if (ESTADO_CANCELADO.equalsIgnoreCase(nuevoEstado) && !ESTADO_CANCELADO.equalsIgnoreCase(estadoAnterior)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                int stockAnterior = producto.getStockActual();
                int stockNuevo = stockAnterior + detalle.getCantidad();
                
                // Restaurar stock
                producto.setStockActual(stockNuevo);
                productoRepository.save(producto);

                // Registrar movimiento en Inventario (Entrada por cancelación)
                Inventario movimiento = Inventario.builder()
                        .producto(producto)
                        .usuario(pedido.getCliente().getUsuario())
                        .tipoMovimiento("entrada")
                        .cantidad(detalle.getCantidad())
                        .stockAnterior(stockAnterior)
                        .stockNuevo(stockNuevo)
                        .fechaMovimiento(LocalDateTime.now())
                        .observaciones("Cancelación de Pedido #" + pedido.getIdPedido())
                        .build();
                inventarioRepository.save(movimiento);
            }
        }

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
                .telefonoPago(pedido.getTelefonoPago())
                .items(detallesDTO)
                .build();
    }
}
