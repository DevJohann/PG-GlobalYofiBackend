package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.EstadoPedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoResponseDTO;
import com.globalyofi.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/realizar")
    @PreAuthorize("isAuthenticated()")
    public PedidoResponseDTO realizarPedido(@RequestBody PedidoRequestDTO request) {
        return pedidoService.realizarPedido(
                request.getClienteId(),
                request.getMetodoPago(),
                request.getDireccion(),
                request.getCiudad()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PedidoResponseDTO> listarPedidos() {
        return pedidoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PedidoResponseDTO obtenerDetallePedido(@PathVariable("id") Integer id) {
        return pedidoService.obtenerPorId(id);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public PedidoResponseDTO actualizarEstado(@PathVariable("id") Integer id, @RequestBody EstadoPedidoRequestDTO request) {
        return pedidoService.actualizarEstado(id, request.getEstado());
    }
}
