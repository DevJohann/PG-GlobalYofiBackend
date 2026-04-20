package com.globalyofi.backend.service;

import com.globalyofi.backend.dto.chatbot.DialogflowRequestDTO;
import com.globalyofi.backend.dto.chatbot.DialogflowResponseDTO;
import com.globalyofi.backend.entity.Pedido;
import com.globalyofi.backend.entity.Producto;
import com.globalyofi.backend.repository.PedidoRepository;
import com.globalyofi.backend.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public ChatbotService(ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public DialogflowResponseDTO getRecomendaciones(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters()
                : new HashMap<>();

        String tipoProducto = getParameterAsString(params, "tipo_producto");
        String tipoPiel = getParameterAsString(params, "tipo_piel");

        List<Producto> productos = productoRepository.buscarParaChatbot(
                tipoProducto,
                tipoPiel,
                PageRequest.of(0, 3));

        if (productos.isEmpty()) {
            return DialogflowResponseDTO.createSimpleResponse(
                    "Lo siento, no encontré productos que coincidan con tu búsqueda en este momento.");
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
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters()
                : new HashMap<>();
        String nombreProducto = getParameterAsString(params, "nombre_producto");

        if (nombreProducto == null || nombreProducto.isBlank()) {
            return DialogflowResponseDTO
                    .createSimpleResponse("Por favor dime el nombre del producto que estás buscando.");
        }

        List<Producto> productos = productoRepository.buscarParaChatbot(
                nombreProducto,
                null,
                PageRequest.of(0, 1));

        if (productos.isEmpty()) {
            return DialogflowResponseDTO
                    .createSimpleResponse("Lo siento, no encontré stock para el producto: " + nombreProducto);
        }

        Producto p = productos.get(0);
        return DialogflowResponseDTO.createSimpleResponse(
                String.format("El producto %s está disponible y cuesta $%s", p.getNombre(), p.getPrecio()));
    }

    public DialogflowResponseDTO consultarPedido(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters()
                : new HashMap<>();
        Object numeroPedidoObj = params.get("numero_pedido");

        if (numeroPedidoObj == null) {
            return DialogflowResponseDTO
                    .createSimpleResponse("Por favor proporciona el número de pedido que deseas consultar.");
        }

        Integer idPedido = null;
        try {
            // Manejar posibles valores Double o Long que Jackson pone en el Map
            if (numeroPedidoObj instanceof Number) {
                idPedido = ((Number) numeroPedidoObj).intValue();
            } else {
                idPedido = Integer.valueOf(numeroPedidoObj.toString());
            }
        } catch (Exception e) {
            logger.error("Error al convertir numero_pedido: {}", e.getMessage());
            return DialogflowResponseDTO.createSimpleResponse("El número de pedido ingresado no es válido.");
        }

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);

        if (pedido == null) {
            return DialogflowResponseDTO
                    .createSimpleResponse("No se encontró ningún pedido con el número: " + idPedido);
        }

        return DialogflowResponseDTO.createSimpleResponse(
                String.format("Tu pedido está en estado: %s", pedido.getEstado()));
    }

    public DialogflowResponseDTO buscarMaquillaje(DialogflowRequestDTO request) {
        Map<String, Object> params = request.getSessionInfo() != null ? request.getSessionInfo().getParameters()
                : new HashMap<>();

        String categoria = getParameterAsString(params, "categoria");
        String marca = getParameterAsString(params, "marca");
        String tipoPiel = getParameterAsString(params, "tipo_piel");

        if (categoria == null || categoria.isBlank()) {
            return DialogflowResponseDTO.createSimpleResponse(
                    "Por favor dime qué categoría de producto estás buscando (por ejemplo: Labios, Rostro, Ojos).");
        }

        List<Producto> productos = productoRepository.buscarPorCategoriaMarcaTipoPiel(
                categoria, marca, tipoPiel, PageRequest.of(0, 5));

        if (productos.isEmpty()) {
            return DialogflowResponseDTO.createSimpleResponse(
                    "Lo siento, no encontré productos de maquillaje con esas características en este momento.");
        }

        return createRichContentResponse(productos, "¡Aquí tienes algunas opciones geniales!");
    }

    public DialogflowResponseDTO manejarIntentDesconocido() {
        return DialogflowResponseDTO.createSimpleResponse(
                "Lo siento, no pude entender tu solicitud. Por favor intenta preguntarme sobre productos, recomendaciones o el estado de tu pedido.");
    }

    // --- Helpers para parámetros y Rich Content ---

    private DialogflowResponseDTO createRichContentResponse(List<Producto> productos, String textMessage) {
        Map<String, Object> payload = new HashMap<>();
        List<List<Map<String, Object>>> richContent = new ArrayList<>();

        for (Producto p : productos) {
            List<Map<String, Object>> cardBundle = new ArrayList<>();
            
            // Componente de información (Imagen + Título + Descripción)
            Map<String, Object> infoCard = new HashMap<>();
            infoCard.put("type", "info");
            infoCard.put("title", p.getNombre());
            infoCard.put("subtitle", String.format("$%.2f - %s", p.getPrecio(), 
                    (p.getDescripcion() != null ? p.getDescripcion() : "Sin descripción")));

            if (p.getImagenUrl() != null && !p.getImagenUrl().isBlank()) {
                Map<String, Object> imageNode = new HashMap<>();
                Map<String, Object> srcNode = new HashMap<>();
                // Nota: rawUrl debería ser una URL absoluta para que Dialogflow la cargue correctamente
                srcNode.put("rawUrl", p.getImagenUrl());
                imageNode.put("src", srcNode);
                infoCard.put("image", imageNode);
            }
            cardBundle.add(infoCard);

            // Componente de botón para ver el producto en la web
            Map<String, Object> button = new HashMap<>();
            button.put("type", "button");
            button.put("text", "Ver producto");
            button.put("icon", Map.of("type", "chevron_right", "color", "#FF9800"));
            button.put("link", "http://localhost:4200/productos/" + p.getIdProducto());
            cardBundle.add(button);

            // Añadir el bundle como una "tarjeta" en el carrusel de richContent
            richContent.add(cardBundle);
        }

        payload.put("richContent", richContent);

        List<DialogflowResponseDTO.Message> messages = new ArrayList<>();
        
        // Mensaje de texto
        messages.add(DialogflowResponseDTO.Message.builder()
                .text(DialogflowResponseDTO.TextMessage.builder()
                        .text(Collections.singletonList(textMessage))
                        .build())
                .build());

        // Mensaje del payload (Rich Content)
        messages.add(DialogflowResponseDTO.Message.builder()
                .payload(payload)
                .build());

        return DialogflowResponseDTO.builder()
                .fulfillmentResponse(DialogflowResponseDTO.FulfillmentResponse.builder()
                        .messages(messages)
                        .build())
                .build();
    }

    private String getParameterAsString(Map<String, Object> params, String key) {
        if (!params.containsKey(key) || params.get(key) == null) {
            return null;
        }
        String value = params.get(key).toString();
        return value.isBlank() ? null : value;
    }
}
