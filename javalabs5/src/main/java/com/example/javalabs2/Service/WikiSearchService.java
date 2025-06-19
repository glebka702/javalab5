package com.example.javalabs2.Service;

import com.example.javalabs2.Dto.WikiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WikiSearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public WikiSearchService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public WikiResponse search(String term, String wikipediaApiUrl) {
        try {
            String url = wikipediaApiUrl + term.replace(" ", "%20");
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode pages = root.path("query").path("pages");
            JsonNode firstPage = pages.elements().next();

            String title = firstPage.path("title").asText();
            String content = firstPage.path("extract").asText();

            if (content.isEmpty()) {
                content = "No information found for '" + term + "' in Wikipedia";
            }

            return new WikiResponse(title, content);

        } catch (Exception e) {
            return new WikiResponse(term, "Error fetching data from Wikipedia: " + e.getMessage());
        }
    }
}