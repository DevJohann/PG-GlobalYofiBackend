package com.globalyofi.backend.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DialogflowRequestDTO {
    
    private FulfillmentInfo fulfillmentInfo;
    private SessionInfo sessionInfo;
    private IntentInfo intentInfo;
    private PageInfo pageInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FulfillmentInfo {
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionInfo {
        private String session;
        private Map<String, Object> parameters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IntentInfo {
        private String lastMatchedIntent;
        private String displayName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private String currentPage;
        private String displayName;
    }
}
