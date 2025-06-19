package com.example.javalabs2.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WikiResponse {
    private String searchTerm;
    private String content;
}