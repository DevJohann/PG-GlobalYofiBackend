package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.CarritoDTO;
import com.globalyofi.backend.dto.CarritoRequestDTO;
import com.globalyofi.backend.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // 1 Obtener carrito activo del cliente
    @GetMapping("/cliente/{clienteId}")
    public CarritoDTO obtenerCarritoActivo(@PathVariable("clienteId") Integer clienteId) {
        return carritoService.obtenerCarritoActivoDTO(clienteId);
    }

    // 2 Agregar producto al carrito
    @PostMapping("/agregar-producto")
    public CarritoDTO agregarProducto(@RequestBody CarritoRequestDTO request) {
        return carritoService.agregarProducto(request.getClienteId(), request.getProductoId(), request.getCantidad());
    }

    // 3 Actualizar cantidad de un item
    @PutMapping("/item/{itemId}")
    public CarritoDTO actualizarCantidad(@PathVariable("itemId") Integer itemId,
            @RequestBody CarritoRequestDTO request) {
        return carritoService.actualizarCantidad(itemId, request.getCantidad());
    }

    // 4 Eliminar producto del carrito
    @DeleteMapping("/item/{itemId}")
    public CarritoDTO eliminarItem(@PathVariable("itemId") Integer itemId) {
        return carritoService.eliminarItem(itemId);
    }

    // 5 Vaciar carrito
    @DeleteMapping("/vaciar/{carritoId}")
    public CarritoDTO vaciarCarrito(@PathVariable("carritoId") Integer carritoId) {
        return carritoService.vaciarCarrito(carritoId);
    }

    // 6 Obtener carrito completo
    @GetMapping("/{carritoId}")
    public CarritoDTO obtenerCarrito(@PathVariable("carritoId") Integer carritoId) {
        return carritoService.obtenerCarritoDTO(carritoId);
    }
}
