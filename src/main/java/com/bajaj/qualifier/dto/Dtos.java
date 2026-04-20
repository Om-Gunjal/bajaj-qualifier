package com.bajaj.qualifier.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Dtos {

    // ── Request body for /generateWebhook ────────────────────────────────
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateWebhookRequest {
        private String name;
        private String regNo;
        private String email;
    }

    // ── Response body from /generateWebhook ──────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerateWebhookResponse {
        private String webhook;
        private String accessToken;
    }

    // ── Request body for /testWebhook ────────────────────────────────────
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmitQueryRequest {
        private String finalQuery;
    }
}
