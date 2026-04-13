package com.globalyofi.backend.controller;

import com.globalyofi.backend.dto.chatbot.DialogflowRequestDTO;
import com.globalyofi.backend.dto.chatbot.DialogflowResponseDTO;
import com.globalyofi.backend.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<DialogflowResponseDTO> handleWebhook(@RequestBody DialogflowRequestDTO request) {
        try {
            logger.debug("Recibiendo webhook de Dialogflow. Request: {}", request);
            
            if (request.getFulfillmentInfo() == null || request.getFulfillmentInfo().getTag() == null) {
                logger.warn("Request de Dialogflow sin tag de cumplimiento.");
                return ResponseEntity.ok(chatbotService.manejarIntentDesconocido());
            }

            String tag = request.getFulfillmentInfo().getTag();
            logger.info("Procesando intent con tag: {}", tag);
            DialogflowResponseDTO response;

            switch (tag) {
                case "recomendar_productos":
                    response = chatbotService.getRecomendaciones(request);
                    break;
                case "consultar_producto":
                    response = chatbotService.consultarProducto(request);
                    break;
                case "estado_pedido":
                    response = chatbotService.consultarPedido(request);
                    break;
                case "buscar_maquillaje":
                    response = chatbotService.buscarMaquillaje(request);
                    break;
                default:
                    response = chatbotService.manejarIntentDesconocido();
                    break;
            }

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.ok(DialogflowResponseDTO.createSimpleResponse(
                    "Ocurrió un error inesperado al procesar tu solicitud: " + e.getMessage()));
        }
    }
}
