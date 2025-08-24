package com.example.operador.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final WebClient gatewayClient;

    public OrderController(@Value("${app.gatewayBaseUrl}") String gatewayBaseUrl) {
        this.gatewayClient = WebClient.create(gatewayBaseUrl);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        String itemId = String.valueOf(payload.get("itemId"));
        Integer quantity = Integer.valueOf(String.valueOf(payload.getOrDefault("quantity", 1)));
        return gatewayClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/items/search").queryParam("q", itemId).build()).retrieve()
                .bodyToMono(String.class).map(searchJson -> Map.of("status", "ORDER_PLACED", "itemId", itemId,
                        "quantity", quantity, "searchProof", searchJson));
    }
}