package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.chatbot.DialogflowRequestDTO;
import com.globalyofi.backend.dto.chatbot.DialogflowResponseDTO;
import com.globalyofi.backend.entity.Pedido;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.repository.PedidoRepository;
import com.globalyofi.backend.repository.ProductoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public ChatbotService(ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public DialogflowResponseDTO getRecomendaciones(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters() : new HashMap<>();
        
        String tipoProducto = getParameterAsString(params, "tipo_producto");
        String tipoPiel = getParameterAsString(params, "tipo_piel");
        BigDecimal presupuesto = getParameterAsBigDecimal(params, "presupuesto");

        List<Producto> productos = productoRepository.buscarParaChatbot(
                tipoProducto, 
                tipoPiel, 
                presupuesto, 
                PageRequest.of(0, 3)
        );

        if (productos.isEmpty()) {
            return DialogflowResponseDTO.createSimpleResponse("Lo siento, no encontré productos que coincidan con tu búsqueda en este momento.");
        }

        List<Map<String, Object>> productosResponse = new ArrayList<>();
        for (Producto p : productos) {
            Map<String, Object> prodData = new HashMap<>();
            prodData.put("nombre", p.getNombre());
            prodData.put("precio", p.getPrecio());
            prodData.put("descripcion", p.getDescripcion() != null ? p.getDescripcion() : "Sin descripción adicional");
            prodData.put("imagen", p.getImagenUrl() != null ? p.getImagenUrl() : "");
            productosResponse.add(prodData);
        }

        DialogflowResponseDTO response = DialogflowResponseDTO.createSimpleResponse("Te recomiendo estos productos:");
        
        // Agregar sessionInfo para Dialogflow
        response.setSessionInfo(DialogflowResponseDTO.SessionInfo.builder()
                .parameters(new HashMap<>())
                .build());
        response.getSessionInfo().getParameters().put("productos", productosResponse);

        return response;
    }

    public DialogflowResponseDTO consultarProducto(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters() : new HashMap<>();
        String nombreProducto = getParameterAsString(params, "nombre_producto");

        if (nombreProducto == null || nombreProducto.isBlank()) {
            return DialogflowResponseDTO.createSimpleResponse("Por favor dime el nombre del producto que estás buscando.");
        }

        List<Producto> productos = productoRepository.buscarParaChatbot(
                nombreProducto, 
                null, 
                null, 
                PageRequest.of(0, 1)
        );

        if (productos.isEmpty()) {
            return DialogflowResponseDTO.createSimpleResponse("Lo siento, no encontré stock para el producto: " + nombreProducto);
        }

        Producto p = productos.get(0);
        return DialogflowResponseDTO.createSimpleResponse(
                String.format("El producto %s está disponible y cuesta $%s", p.getNombre(), p.getPrecio())
        );
    }

    public DialogflowResponseDTO consultarPedido(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters() : new HashMap<>();
        Object numeroPedidoObj = params.get("numero_pedido");

        if (numeroPedidoObj == null) {
            return DialogflowResponseDTO.createSimpleResponse("Por favor proporciona el número de pedido que deseas consultar.");
        }

        Integer idPedido = null;
        try {
            idPedido = Integer.valueOf(numeroPedidoObj.toString());
        } catch (NumberFormatException e) {
            return DialogflowResponseDTO.createSimpleResponse("El número de pedido ingresado no es válido.");
        }

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);

        if (pedido == null) {
            return DialogflowResponseDTO.createSimpleResponse("No se encontró ningún pedido con el número: " + idPedido);
        }

        return DialogflowResponseDTO.createSimpleResponse(
                String.format("Tu pedido está en estado: %s", pedido.getEstado())
        );
    }

    public DialogflowResponseDTO manejarIntentDesconocido() {
        return DialogflowResponseDTO.createSimpleResponse("Lo siento, no pude entender tu solicitud. Por favor intenta preguntarme sobre productos, recomendaciones o el estado de tu pedido.");
    }

    // --- Helpers para parámetros de Dialogflow ---
    
    private String getParameterAsString(Map<String, Object> params, String key) {
        if (!params.containsKey(key) || params.get(key) == null) {
            return null;
        }
        String value = params.get(key).toString();
        return value.isBlank() ? null : value;
    }

    private BigDecimal getParameterAsBigDecimal(Map<String, Object> params, String key) {
        if (!params.containsKey(key) || params.get(key) == null) {
            return null;
        }
        try {
            return new BigDecimal(params.get(key).toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
