package com.globalyofi.backend.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DialogflowResponseDTO {

    private FulfillmentResponse fulfillment_response;
    private SessionInfo sessionInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FulfillmentResponse {
        private List<Message> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private TextMessage text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextMessage {
        private List<String> text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SessionInfo {
        private Map<String, Object> parameters;
    }

    // Constructor helper to create a simple text response
    public static DialogflowResponseDTO createSimpleResponse(String textMessage) {
        return DialogflowResponseDTO.builder()
                .fulfillment_response(FulfillmentResponse.builder()
                        .messages(Collections.singletonList(Message.builder()
                                .text(TextMessage.builder()
                                        .text(Collections.singletonList(textMessage))
                                        .build())
                                .build()))
                        .build())
                .build();
    }
}
