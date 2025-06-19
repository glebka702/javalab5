package com.example.javalabs2.Controller;

import com.example.javalabs2.Dto.WikiResponse;
import com.example.javalabs2.Service.WikiSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WikiSearchController {

    private final WikiSearchService wikiSearchService;

    @Value("${wikipedia.api.url}")
    private String wikipediaApiUrl;

    public WikiSearchController(WikiSearchService wikiSearchService) {
        this.wikiSearchService = wikiSearchService;
    }

    @GetMapping("/api/search")
    public ResponseEntity<WikiResponse> search(@RequestParam String term) {
        return ResponseEntity.ok(wikiSearchService.search(term, wikipediaApiUrl));
    }
}