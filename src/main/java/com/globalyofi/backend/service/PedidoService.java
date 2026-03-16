package com.globalyofi.backend.service;

import com.globalyofi.backend.entity.*;
import com.globalyofi.backend.repository.CarritoRepository;
import com.globalyofi.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;

    @Transactional
    public Pedido realizarPedido(Integer clienteId, String metodoPago, String direccion, String ciudad) {
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

        if (cliente.getDireccion() == null || cliente.getDireccion().isEmpty() ||
            cliente.getCiudad() == null || cliente.getCiudad().isEmpty()) {
            throw new RuntimeException("Por favor, verifica que tu perfil de cliente esté completo (dirección y ciudad) antes de realizar el pedido.");
        }

        // 2. Crear el Pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .fechaPedido(LocalDateTime.now())
                .total(carrito.getTotalEstimado())
                .estado("PENDIENTE")
                .metodoPago(metodoPago)
                .ciudadEnvio(cliente.getCiudad())
                .direccionEnvio(cliente.getDireccion())
                .build();

        // 3. Crear detalles del pedido
        List<DetallePedido> detalles = carrito.getItems().stream().map(item -> DetallePedido.builder()
                .pedido(pedido)
                .producto(item.getProducto())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getProducto().getPrecio())
                .subtotal(item.getProducto().getPrecio().multiply(java.math.BigDecimal.valueOf(item.getCantidad())))
                .build()).collect(Collectors.toList());

        pedido.setDetalles(detalles);

        // 4. Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 5. Vaciar/Desactivar carrito (en este caso lo marcamos como 'completado' o simplemente vaciamos items)
        // Para este proyecto, parece que los carritos se mantienen pero se pueden vaciar.
        // Vamos a marcar el estado del carrito como 'procesado' y dejar que el service cree uno nuevo si se pide.
        carrito.setEstado("procesado");
        carritoRepository.save(carrito);

        return pedidoGuardado;
    }
}
