package com.globalyofi.backend.controller;

import com.globalyofi.backend.entity.Pedido;
import com.globalyofi.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/realizar")
    public Pedido realizarPedido(@RequestBody Map<String, Object> request) {
        Integer clienteId = (Integer) request.get("clienteId");
        String metodoPago = (String) request.get("metodoPago");
        String direccion = (String) request.get("direccion");
        String ciudad = (String) request.get("ciudad");
        return pedidoService.realizarPedido(clienteId, metodoPago, direccion, ciudad);
    }
}
