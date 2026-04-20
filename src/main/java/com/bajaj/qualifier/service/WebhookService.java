package com.bajaj.qualifier.service;

import com.bajaj.qualifier.dto.Dtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final RestTemplate restTemplate;

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    /*
     * REG: ADT24SOCBD077  →  last two digits = 77  →  ODD  →  Question 1
     *
     * Question 1 – SQL Problem:
     * -----------------------------------------------------------------
     * Given tables:
     *   EMPLOYEES  (ID, NAME, AMOUNT, DEPARTMENT_ID, MANAGER_ID)
     *   DEPARTMENTS(ID, NAME)
     *
     * Task: Find employees who earn more than their manager.
     *       Return: employee name, amount, department name, manager name
     *       Order:  by employee amount DESC
     * -----------------------------------------------------------------
     */
    private static final String FINAL_SQL =
            "SELECT e.NAME AS EMPLOYEE_NAME, " +
            "e.AMOUNT AS EMPLOYEE_AMOUNT, " +
            "d.NAME AS DEPARTMENT_NAME, " +
            "m.NAME AS MANAGER_NAME " +
            "FROM EMPLOYEES e " +
            "JOIN EMPLOYEES m ON e.MANAGER_ID = m.ID " +
            "JOIN DEPARTMENTS d ON e.DEPARTMENT_ID = d.ID " +
            "WHERE e.AMOUNT > m.AMOUNT " +
            "ORDER BY e.AMOUNT DESC";

    // ── Triggered automatically when Spring Boot is fully ready ───────────
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        log.info("=== Bajaj Qualifier Flow Started ===");

        try {
            // STEP 1 – Generate webhook
            Dtos.GenerateWebhookResponse webhookResponse = generateWebhook();
            if (webhookResponse == null) {
                log.error("Failed to get webhook response. Aborting.");
                return;
            }

            String webhookUrl  = webhookResponse.getWebhook();
            String accessToken = webhookResponse.getAccessToken();

            log.info("Webhook URL   : {}", webhookUrl);
            log.info("Access Token  : {}", accessToken);

            // STEP 2 – Submit SQL answer
            submitAnswer(webhookUrl, accessToken);

        } catch (Exception e) {
            log.error("Unexpected error in qualifier flow: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // STEP 1: POST /generateWebhook
    // ─────────────────────────────────────────────────────────────────────
    private Dtos.GenerateWebhookResponse generateWebhook() {
        Dtos.GenerateWebhookRequest requestBody = new Dtos.GenerateWebhookRequest(
                "Omkar Gunjal",
                "ADT24SOCBD077",
                "imomgunjal@gmail.com"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Dtos.GenerateWebhookRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Calling generateWebhook API...");
            ResponseEntity<Dtos.GenerateWebhookResponse> response =
                    restTemplate.exchange(
                            GENERATE_URL,
                            HttpMethod.POST,
                            entity,
                            Dtos.GenerateWebhookResponse.class
                    );

            log.info("generateWebhook response status: {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error calling generateWebhook: {}", e.getMessage(), e);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // STEP 2: POST to webhook URL with JWT + SQL answer
    // ─────────────────────────────────────────────────────────────────────
    private void submitAnswer(String webhookUrl, String accessToken) {
        Dtos.SubmitQueryRequest requestBody = new Dtos.SubmitQueryRequest(FINAL_SQL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // JWT token — sent as Bearer token per standard convention
        headers.set("Authorization", accessToken);

        HttpEntity<Dtos.SubmitQueryRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Submitting SQL answer to webhook...");
            log.info("SQL Query: {}", FINAL_SQL);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            webhookUrl,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            log.info("=== Submission Response ===");
            log.info("Status : {}", response.getStatusCode());
            log.info("Body   : {}", response.getBody());
            log.info("=== Qualifier Flow Complete ===");

        } catch (Exception e) {
            log.error("Error submitting answer: {}", e.getMessage(), e);
        }
    }
}
