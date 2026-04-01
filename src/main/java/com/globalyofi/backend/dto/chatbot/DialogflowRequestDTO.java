package com.globalyofi.backend.dto.chatbot;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogflowRequestDTO {
    
    private FulfillmentInfo fulfillmentInfo;
    private SessionInfo sessionInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FulfillmentInfo {
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private String session;
        private Map<String, Object> parameters;
    }
}
