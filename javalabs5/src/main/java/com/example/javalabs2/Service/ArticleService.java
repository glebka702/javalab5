package com.example.javalabs2.Service;

import com.example.javalabs2.Entity.Article;
import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Repository.ArticleRepository;
import com.example.javalabs2.Repository.CommentRepository;
import com.example.javalabs2.Cache.ArticleCache;
import com.example.javalabs2.Cache.CommentCache;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ArticleCache articleCache;
    private final CommentCache commentCache;

    public ArticleService(ArticleRepository articleRepository,
                          CommentRepository commentRepository,
                          ArticleCache articleCache,
                          CommentCache commentCache) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.articleCache = articleCache;
        this.commentCache = commentCache;
    }

    public Article createArticle(Article article) {
        Article saved = articleRepository.save(article);
        articleCache.putArticle(saved);
        return saved;
    }

    public List<Article> createArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            throw new IllegalArgumentException("Article list cannot be null or empty");
        }
        List<Article> savedArticles = articleRepository.saveAll(articles);
        savedArticles.forEach(articleCache::putArticle);
        return savedArticles;
    }

    public List<Article> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        articles.forEach(articleCache::putArticle);
        return articles;
    }

    public Article getArticleById(Long id) {
        Article cached = articleCache.getArticleById(id);
        if (cached != null) {
            return cached;
        }
        return articleRepository.findById(id)
                .map(article -> {
                    articleCache.putArticle(article);
                    return article;
                })
                .orElse(null);
    }

    public Article updateArticle(Long id, Article articleDetails) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return null;
        }
        Article article = articleOpt.get();
        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        Article updated = articleRepository.save(article);
        articleCache.putArticle(updated);
        return updated;
    }

    @Transactional
    public boolean deleteArticle(Long id) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isEmpty()) {
            return false;
        }
        Article article = articleOpt.get();
        article.getComments().forEach(comment -> commentCache.removeComment(comment.getId()));
        articleRepository.delete(article);
        articleCache.removeArticle(id);
        return true;
    }

    @Transactional
    public Comment addComment(Long articleId, Comment comment) {
        Optional<Article> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            return null;
        }
        Article article = articleOpt.get();
        comment.setArticle(article);
        article.getComments().add(comment);
        Comment saved = commentRepository.save(comment);
        commentCache.putComment(saved);
        articleCache.putArticle(article);
        return saved;
    }

    public List<Comment> getArticleComments(Long articleId) {
        List<Comment> cached = commentCache.getCommentsByArticle(articleId);
        if (!cached.isEmpty()) {
            return cached;
        }
        List<Comment> comments = commentRepository.findByArticleId(articleId);
        comments.forEach(commentCache::putComment);
        return comments;
    }
}