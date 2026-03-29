package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.PagoResponseDTO;
import com.globalyofi.backend.entity.Pago;
import com.globalyofi.backend.entity.Pedido;
import com.globalyofi.backend.repository.PagoRepository;
import com.globalyofi.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    @Value("${uploads.dir:uploads}")
    private String uploadsDir;

    @Transactional
    public PagoResponseDTO iniciarPago(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        // Avoid duplicate pago records (idempotent)
        return pagoRepository.findByPedidoIdPedido(pedidoId)
            .map(this::mapToDTO)
            .orElseGet(() -> {
                Pago pago = Pago.builder()
                    .pedido(pedido)
                    .metodo(pedido.getMetodoPago())
                    .estado("PENDIENTE")
                    .referencia("PEDIDO-" + pedidoId)
                    .build();
                return mapToDTO(pagoRepository.save(pago));
            });
    }

    @Transactional
    public PagoResponseDTO subirComprobante(Integer pedidoId, MultipartFile file) throws IOException {
        Pago pago = pagoRepository.findByPedidoIdPedido(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado para pedido: " + pedidoId));

        // Save file to disk
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadsDir, "comprobantes");
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        pago.setComprobanteUrl("/uploads/comprobantes/" + filename);
        pago.setEstado("VERIFICACION");

        // Update order status
        Pedido pedido = pago.getPedido();
        pedido.setEstado(PedidoService.ESTADO_PENDIENTE_VERIFICACION);
        pedidoRepository.save(pedido);

        return mapToDTO(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponseDTO validarPago(Integer pedidoId) {
        Pago pago = pagoRepository.findByPedidoIdPedido(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado para pedido: " + pedidoId));

        pago.setEstado("VALIDADO");

        // Advance order to PAGADO
        Pedido pedido = pago.getPedido();
        pedido.setEstado(PedidoService.ESTADO_PAGADO);
        pedidoRepository.save(pedido);

        return mapToDTO(pagoRepository.save(pago));
    }

    public PagoResponseDTO obtenerPago(Integer pedidoId) {
        Pago pago = pagoRepository.findByPedidoIdPedido(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado para pedido: " + pedidoId));
        return mapToDTO(pago);
    }

    private PagoResponseDTO mapToDTO(Pago pago) {
        Pedido pedido = pago.getPedido();
        return PagoResponseDTO.builder()
            .idPago(pago.getIdPago())
            .pedidoId(pedido.getIdPedido())
            .metodo(pago.getMetodo())
            .estado(pago.getEstado())
            .referencia(pago.getReferencia())
            .comprobanteUrl(pago.getComprobanteUrl())
            .fecha(pago.getFecha())
            .totalPedido(pedido.getTotal())
            .estadoPedido(pedido.getEstado())
            .nombreCliente(pedido.getCliente().getUsuario().getNombre())
            .emailCliente(pedido.getCliente().getUsuario().getEmail())
            .build();
    }
}
