package com.example.buscador.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class SearchController {

    private final RestClient es;
    private final String index;

    public SearchController(@Value("${spring.elasticsearch.uris}") String esUris,
                            @Value("${app.search.index:items}") String index) {
        // Si vienen varias URIs separadas por coma, toma la primera
        String base = esUris.split(",")[0].trim();
        this.es = RestClient.builder().baseUrl(base).build();
        this.index = index;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> search(@RequestParam String q) {
        Map<String, Object> body = (q == null || q.isBlank())
                ? Map.of("query", Map.of("match_all", Map.of()), "size", 20)
                : Map.of(
                    "query", Map.of(
                        "multi_match", Map.of(
                            "query", q,
                            "fields", List.of("title^3", "title._2gram", "title._3gram", "description")
                        )
                    ),
                    "size", 20
                );

        String res = es.post()
                .uri("/{index}/_search", index)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> suggest(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\":\"q must not be blank\"}");
        }

        Map<String, Object> body = Map.of(
            "query", Map.of(
                "multi_match", Map.of(
                    "query", q,
                    "type", "bool_prefix",
                    "fields", List.of("title", "title._2gram", "title._3gram")
                )
            ),
            "size", 10
        );

        String res = es.post()
                .uri("/{index}/_search", index)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/facets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> facets() {
        Map<String, Object> body = Map.of(
            "size", 0,
            "aggs", Map.of(
                "by_category", Map.of("terms", Map.of("field", "category.keyword", "size", 20)),
                "by_tags",     Map.of("terms", Map.of("field", "tags.keyword",     "size", 20)),
                "price_ranges", Map.of(
                    "range", Map.of(
                        "field", "price",
                        "ranges", List.of(
                            Map.of("to", 5),
                            Map.of("from", 5, "to", 15),
                            Map.of("from", 15)
                        )
                    )
                )
            )
        );

        String res = es.post()
                .uri("/{index}/_search", index)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(res);
    }
}
