package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.EstadoPedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoRequestDTO;
import com.globalyofi.backend.dto.PedidoResponseDTO;
import com.globalyofi.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/realizar")
    @PreAuthorize("isAuthenticated()")
    public PedidoResponseDTO realizarPedido(@Valid @RequestBody PedidoRequestDTO request) {
        return pedidoService.realizarPedido(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PedidoResponseDTO> listarPedidos() {
        return pedidoService.obtenerTodos();
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("isAuthenticated()")
    public List<PedidoResponseDTO> listarMisPedidos(Authentication authentication) {
        String email = authentication.getName();
        return pedidoService.obtenerMisPedidos(email);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PedidoResponseDTO obtenerDetallePedido(@PathVariable("id") Integer id) {
        return pedidoService.obtenerPorId(id);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #request.estado == 'Cancelado' and @pedidoService.esDuenioDelPedido(#id, authentication.name))")
    public PedidoResponseDTO actualizarEstado(@PathVariable("id") Integer id,
            @RequestBody EstadoPedidoRequestDTO request) {
        return pedidoService.actualizarEstado(id, request.getEstado());
    }
}
