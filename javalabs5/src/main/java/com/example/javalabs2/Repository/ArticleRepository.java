package com.example.javalabs2.Repository;

import com.example.javalabs2.Entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}