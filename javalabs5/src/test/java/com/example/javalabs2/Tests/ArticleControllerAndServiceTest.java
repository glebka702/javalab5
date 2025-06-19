package com.example.javalabs2.Tests;

import com.example.javalabs2.Controller.ArticleController;
import com.example.javalabs2.Entity.Article;
import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Exception.GlobalExceptionHandler;
import com.example.javalabs2.Service.ArticleService;
import com.example.javalabs2.Repository.ArticleRepository;
import com.example.javalabs2.Repository.CommentRepository;
import com.example.javalabs2.Cache.ArticleCache;
import com.example.javalabs2.Cache.CommentCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ArticleControllerAndServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleCache articleCache;

    @Mock
    private CommentCache commentCache;

    @InjectMocks
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private Article article;

    @Mock
    private Comment comment;

    @Mock
    private Article invalidArticle;

    @Mock
    private Comment invalidComment;

    @Mock
    private List<Comment> commentList;

    @BeforeEach
    void shouldSetUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(articleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        when(article.getId()).thenReturn(1L);
        when(article.getTitle()).thenReturn("Test Article");
        when(article.getContent()).thenReturn("Test Content");
        when(article.getComments()).thenReturn(commentList);
        when(commentList.isEmpty()).thenReturn(true);

        when(comment.getId()).thenReturn(1L);
        when(comment.getAuthor()).thenReturn("Test Author");
        when(comment.getText()).thenReturn("Test Comment");
        when(comment.getArticle()).thenReturn(article);

        when(invalidArticle.getTitle()).thenReturn("");
        when(invalidArticle.getContent()).thenReturn("");

        when(invalidComment.getAuthor()).thenReturn("");
        when(invalidComment.getText()).thenReturn("");
    }

    @Test
    void shouldCreateArticle() {
        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article result = articleService.createArticle(article);

        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        verify(articleRepository).save(article);
        verify(articleCache).putArticle(article);
    }

    @Test
    void shouldCreateArticlesInBulk() {
        List<Article> articles = Arrays.asList(article, mock(Article.class));
        when(articleRepository.saveAll(anyList())).thenReturn(articles);

        List<Article> result = articleService.createArticles(articles);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(articleRepository).saveAll(articles);
        verify(articleCache, times(2)).putArticle(any(Article.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForEmptyArticleList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> articleService.createArticles(Collections.emptyList()));
        assertEquals("Article list cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldGetAllArticles() {
        List<Article> articles = Collections.singletonList(article);
        when(articleRepository.findAll()).thenReturn(articles);

        List<Article> result = articleService.getAllArticles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
        verify(articleRepository).findAll();
    }

    @Test
    void shouldGetArticleByIdFromCache() {
        when(articleCache.getArticleById(1L)).thenReturn(article);

        Article result = articleService.getArticleById(1L);

        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        verify(articleCache).getArticleById(1L);
        verify(articleRepository, never()).findById(anyLong());
    }

    @Test
    void shouldGetArticleByIdFromRepository() {
        when(articleCache.getArticleById(1L)).thenReturn(null);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        Article result = articleService.getArticleById(1L);

        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        verify(articleCache).getArticleById(1L);
        verify(articleRepository).findById(1L);
        verify(articleCache).putArticle(article);
    }

    @Test
    void shouldReturnNullWhenArticleNotFoundById() {
        when(articleCache.getArticleById(1L)).thenReturn(null);
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        Article result = articleService.getArticleById(1L);

        assertNull(result);
        verify(articleCache).getArticleById(1L);
        verify(articleRepository).findById(1L);
    }

    @Test
    void shouldUpdateArticle() {
        Article updatedDetails = mock(Article.class);
        when(updatedDetails.getTitle()).thenReturn("Updated Title");
        when(updatedDetails.getContent()).thenReturn("Updated Content");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article result = articleService.updateArticle(1L, updatedDetails);

        assertNotNull(result);
        verify(article).setTitle("Updated Title");
        verify(article).setContent("Updated Content");
        verify(articleRepository).findById(1L);
        verify(articleRepository).save(article);
        verify(articleCache).putArticle(article);
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistentArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        Article result = articleService.updateArticle(1L, article);

        assertNull(result);
        verify(articleRepository).findById(1L);
        verify(articleRepository, never()).save(any());
    }

    @Test
    void shouldDeleteArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentCache.getCommentsByArticle(1L)).thenReturn(Collections.singletonList(comment));

        boolean result = articleService.deleteArticle(1L);

        assertTrue(result);
        verify(articleRepository).findById(1L);
        verify(articleRepository).delete(article);
        verify(articleCache).removeArticle(1L);
        verify(commentCache).removeComment(1L);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = articleService.deleteArticle(1L);

        assertFalse(result);
        verify(articleRepository).findById(1L);
        verify(articleRepository, never()).delete(any());
    }

    @Test
    void shouldAddCommentToArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(article.getComments()).thenReturn(mock(List.class));

        Comment result = articleService.addComment(1L, comment);

        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        verify(articleRepository).findById(1L);
        verify(commentRepository).save(comment);
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldReturnNullWhenAddingCommentToNonExistentArticle() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        Comment result = articleService.addComment(1L, comment);

        assertNull(result);
        verify(articleRepository).findById(1L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldGetArticleCommentsFromCache() {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentCache.getCommentsByArticle(1L)).thenReturn(comments);

        List<Comment> result = articleService.getArticleComments(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
        verify(commentCache).getCommentsByArticle(1L);
        verify(commentRepository, never()).findByArticleId(anyLong());
    }

    @Test
    void shouldGetArticleCommentsFromRepository() {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentCache.getCommentsByArticle(1L)).thenReturn(Collections.emptyList());
        when(commentRepository.findByArticleId(1L)).thenReturn(comments);

        List<Comment> result = articleService.getArticleComments(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
        verify(commentCache).getCommentsByArticle(1L);
        verify(commentRepository).findByArticleId(1L);
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldCreateArticleViaController() throws Exception {
        when(articleService.createArticle(any(Article.class))).thenReturn(article);

        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(article)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(articleService).createArticle(any(Article.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidArticleDataViaController() throws Exception {
        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidArticle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed: {title=Title cannot be empty, content=Content cannot be empty}"));

        verify(articleService, never()).createArticle(any());
    }

    @Test
    void shouldCreateArticlesInBulkViaController() throws Exception {
        List<Article> articles = Arrays.asList(article, mock(Article.class));
        when(articleService.createArticles(anyList())).thenReturn(articles);

        mockMvc.perform(post("/api/articles/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Article"));

        verify(articleService).createArticles(anyList());
    }

    @Test
    void shouldReturnBadRequestForEmptyBulkArticleListViaController() throws Exception {
        when(articleService.createArticles(anyList())).thenThrow(new IllegalArgumentException("Article list cannot be null or empty"));

        mockMvc.perform(post("/api/articles/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.emptyList())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Article list cannot be null or empty"));

        verify(articleService).createArticles(anyList());
    }

    @Test
    void shouldGetAllArticlesViaController() throws Exception {
        List<Article> articles = Collections.singletonList(article);
        when(articleService.getAllArticles()).thenReturn(articles);

        mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Article"));

        verify(articleService).getAllArticles();
    }

    @Test
    void shouldGetArticleByIdViaController() throws Exception {
        when(articleService.getArticleById(1L)).thenReturn(article);

        mockMvc.perform(get("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"));

        verify(articleService).getArticleById(1L);
    }

    @Test
    void shouldReturnNotFoundForNonExistentArticleByIdViaController() throws Exception {
        when(articleService.getArticleById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(articleService).getArticleById(1L);
    }

    @Test
    void shouldUpdateArticleViaController() throws Exception {
        when(articleService.updateArticle(anyLong(), any(Article.class))).thenReturn(article);

        mockMvc.perform(put("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(article)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Article"));

        verify(articleService).updateArticle(eq(1L), any(Article.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentArticleViaController() throws Exception {
        when(articleService.updateArticle(anyLong(), any(Article.class))).thenReturn(null);

        mockMvc.perform(put("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(article)))
                .andExpect(status().isNotFound());

        verify(articleService).updateArticle(eq(1L), any(Article.class));
    }

    @Test
    void shouldDeleteArticleViaController() throws Exception {
        when(articleService.deleteArticle(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(articleService).deleteArticle(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentArticleViaController() throws Exception {
        when(articleService.deleteArticle(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(articleService).deleteArticle(1L);
    }

    @Test
    void shouldAddCommentToArticleViaController() throws Exception {
        when(articleService.addComment(anyLong(), any(Comment.class))).thenReturn(comment);

        mockMvc.perform(post("/api/articles/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test Comment"));

        verify(articleService).addComment(eq(1L), any(Comment.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidCommentDataViaController() throws Exception {
        mockMvc.perform(post("/api/articles/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed: {author=Author cannot be empty, text=Text cannot be empty}"));

        verify(articleService, never()).addComment(anyLong(), any());
    }

    @Test
    void shouldReturnNotFoundWhenAddingCommentToNonExistentArticleViaController() throws Exception {
        when(articleService.addComment(anyLong(), any(Comment.class))).thenReturn(null);

        mockMvc.perform(post("/api/articles/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());

        verify(articleService).addComment(eq(1L), any(Comment.class));
    }

    @Test
    void shouldGetArticleCommentsViaController() throws Exception {
        List<Comment> comments = Collections.singletonList(comment);
        when(articleService.getArticleComments(1L)).thenReturn(comments);

        mockMvc.perform(get("/api/articles/1/comments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Test Comment"));

        verify(articleService).getArticleComments(1L);
    }
}