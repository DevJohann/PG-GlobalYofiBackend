package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.CarritoDTO;
import com.globalyofi.backend.dto.ItemCarritoDTO;
import com.globalyofi.backend.entity.Carrito;
import com.globalyofi.backend.entity.Cliente;
import com.globalyofi.backend.entity.ItemCarrito;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.repository.CarritoRepository;
import com.globalyofi.backend.repository.ClienteRepository;
import com.globalyofi.backend.repository.ItemCarritoRepository;
import com.globalyofi.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public Carrito obtenerOCrearCarritoActivo(Integer clienteId) {
        return carritoRepository.findByClienteIdAndEstado(clienteId, "activo")
                .orElseGet(() -> {
                    Cliente cliente = clienteRepository.findById(clienteId)
                            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
                    Carrito nuevoCarrito = Carrito.builder()
                            .cliente(cliente)
                            .estado("activo")
                            .totalEstimado(BigDecimal.ZERO)
                            .build();
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Transactional
    public CarritoDTO agregarProducto(Integer clienteId, Integer productoId, Integer cantidad) {
        Carrito carrito = obtenerOCrearCarritoActivo(clienteId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getIdProducto().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            ItemCarrito nuevoItem = ItemCarrito.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .cantidad(cantidad)
                    .build();
            carrito.getItems().add(nuevoItem);
        }

        actualizarTotalCarrito(carrito);
        carritoRepository.save(carrito);
        return mappedToDTO(carrito);
    }

    @Transactional
    public CarritoDTO actualizarCantidad(Integer itemId, Integer nuevaCantidad) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        item.setCantidad(nuevaCantidad);
        Carrito carrito = item.getCarrito();
        actualizarTotalCarrito(carrito);
        carritoRepository.save(carrito);
        return mappedToDTO(carrito);
    }

    @Transactional
    public CarritoDTO eliminarItem(Integer itemId) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        Carrito carrito = item.getCarrito();
        carrito.getItems().remove(item);
        itemCarritoRepository.delete(item);

        actualizarTotalCarrito(carrito);
        carritoRepository.save(carrito);
        return mappedToDTO(carrito);
    }

    @Transactional
    public CarritoDTO vaciarCarrito(Integer carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        carrito.getItems().clear();
        actualizarTotalCarrito(carrito);
        return mappedToDTO(carritoRepository.save(carrito));
    }

    public CarritoDTO obtenerCarritoActivoDTO(Integer clienteId) {
        Carrito carrito = obtenerOCrearCarritoActivo(clienteId);
        return mappedToDTO(carrito);
    }

    public CarritoDTO obtenerCarritoDTO(Integer carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
        return mappedToDTO(carrito);
    }

    private void actualizarTotalCarrito(Carrito carrito) {
        BigDecimal total = carrito.getItems().stream()
                .map(item -> item.getProducto().getPrecio().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        carrito.setTotalEstimado(total);
    }

    private CarritoDTO mappedToDTO(Carrito carrito) {
        List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                .map(item -> ItemCarritoDTO.builder()
                        .itemId(item.getIdItem())
                        .productoId(item.getProducto().getIdProducto())
                        .nombreProducto(item.getProducto().getNombre())
                        .precio(item.getProducto().getPrecio())
                        .cantidad(item.getCantidad())
                        .subtotal(item.getProducto().getPrecio().multiply(new BigDecimal(item.getCantidad())))
                        .build())
                .collect(Collectors.toList());

        return CarritoDTO.builder()
                .carritoId(carrito.getIdCarrito())
                .clienteId(carrito.getCliente().getIdCliente())
                .items(itemsDTO)
                .totalEstimado(carrito.getTotalEstimado())
                .build();
    }
}
