package com.example.javalabs2.Cache;

import com.example.javalabs2.Entity.Article;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ArticleCache {
    private final Map<Long, Article> articleByIdMap = new HashMap<>();
    private final Map<String, Article> articleByTitleMap = new HashMap<>();

    public Article getArticleById(Long id) {
        return articleByIdMap.get(id);
    }

    public void putArticle(Article article) {
        articleByIdMap.put(article.getId(), article);
        articleByTitleMap.put(article.getTitle().toLowerCase(), article);
    }

    public void removeArticle(Long id) {
        Article article = articleByIdMap.remove(id);
        if (article != null) {
            articleByTitleMap.remove(article.getTitle().toLowerCase());
        }
    }

}