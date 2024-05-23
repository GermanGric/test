package com.example.crpt;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private String authToken;

    public CrptApi(TimeUnit timeUnit, int requestLimit, String authToken) {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        semaphore = new Semaphore(requestLimit);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(semaphore::release, 0, 1, timeUnit);
        this.authToken = authToken;
    }

    public void updateAuthToken(String newAuthToken) {
        this.authToken = newAuthToken;
    }

    public String createDocument(Object document, String signature) throws IOException, InterruptedException {
        semaphore.acquire();

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("description", objectMapper.valueToTree(document));
        rootNode.put("doc_id", "string");
        rootNode.put("doc_status", "string");
        rootNode.put("doc_type", "LP_INTRODUCE_GOODS");
        rootNode.put("importRequest", true);
        rootNode.put("owner_inn", "string");
        rootNode.put("participant_inn", "string");
        rootNode.put("producer_inn", "string");
        rootNode.put("production_date", "2020-01-23");
        rootNode.put("production_type", "string");
        rootNode.putArray("products")
                .addObject()
                .put("certificate_document", "string")
                .put("certificate_document_date", "2020-01-23")
                .put("certificate_document_number", "string")
                .put("owner_inn", "string")
                .put("producer_inn", "string")
                .put("production_date", "2020-01-23")
                .put("tnved_code", "string")
                .put("uit_code", "string")
                .put("uitu_code", "string");
        rootNode.put("reg_date", "2020-01-23");
        rootNode.put("reg_number", "string");

        String requestBody = objectMapper.writeValueAsString(rootNode);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)  // Добавление заголовка аутентификации
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String authToken = "your_auth_token_here";  // Вставьте ваш токен аутентификации
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5, authToken);

        // Example document
        Object document = new Object() {
            public String participantInn = "string";
        };

        String signature = "example_signature";

        String response = api.createDocument(document, signature);
        System.out.println(response);

        String newAuthToken = "your_new_auth_token_here";
        api.updateAuthToken(newAuthToken);
        response = api.createDocument(document, signature);
        System.out.println(response);
    }
}
