package com.example.javalabs2.Controller;

import com.example.javalabs2.Entity.Article;
import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Article API", description = "Endpoints for managing articles and comments")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "Create a new article", description = "Creates a new article with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid article data"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleService.createArticle(article));
    }

    @Operation(summary = "Get all articles", description = "Retrieves a list of all articles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of articles retrieved"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @Operation(summary = "Get article by ID", description = "Retrieves an article by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article found"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @Operation(summary = "Update an article", description = "Updates an existing article by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Article updated successfully"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "400", description = "Invalid article data"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(
            @PathVariable Long id,
            @RequestBody Article articleDetails) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDetails));
    }

    @Operation(summary = "Delete an article", description = "Deletes an article by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Article deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add a comment to an article", description = "Adds a comment to the specified article")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment added successfully"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "400", description = "Invalid comment data"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/{articleId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long articleId,
            @RequestBody Comment comment) {
        return ResponseEntity.ok(articleService.addComment(articleId, comment));
    }

    @Operation(summary = "Get comments for an article", description = "Retrieves all comments for the specified article")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of comments retrieved"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/{articleId}/comments")
    public ResponseEntity<List<Comment>> getArticleComments(
            @PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.getArticleComments(articleId));
    }
}