package com.example.javalabs2.Tests;

import com.example.javalabs2.Controller.CommentController;
import com.example.javalabs2.Entity.Article;
import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Exception.GlobalExceptionHandler;
import com.example.javalabs2.Service.CommentService;
import com.example.javalabs2.Repository.CommentRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerAndServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentCache commentCache;

    @InjectMocks
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private Comment comment;

    @Mock
    private Article article;

    @Mock
    private Comment invalidComment;

    @BeforeEach
    void shouldSetUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        when(article.getId()).thenReturn(1L);
        when(article.getTitle()).thenReturn("Test Article");
        when(article.getContent()).thenReturn("Test Content");

        when(comment.getId()).thenReturn(1L);
        when(comment.getAuthor()).thenReturn("Test Author");
        when(comment.getText()).thenReturn("Test Comment");
        when(comment.getArticle()).thenReturn(article);

        when(invalidComment.getAuthor()).thenReturn("");
        when(invalidComment.getText()).thenReturn("");
    }

    @Test
    void shouldCreateComment() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(comment);

        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        verify(commentRepository).save(comment);
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldGetAllComments() {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentRepository.findAll()).thenReturn(comments);

        List<Comment> result = commentService.getAllComments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
        verify(commentRepository).findAll();
    }

    @Test
    void shouldGetCommentByIdFromCache() {
        when(commentCache.getCommentById(1L)).thenReturn(comment);

        Comment result = commentService.getCommentById(1L);

        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        verify(commentCache).getCommentById(1L);
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    void shouldGetCommentByIdFromRepository() {
        when(commentCache.getCommentById(1L)).thenReturn(null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(1L);

        assertNotNull(result);
        assertEquals("Test Comment", result.getText());
        verify(commentCache).getCommentById(1L);
        verify(commentRepository).findById(1L);
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldReturnNullWhenCommentNotFoundById() {
        when(commentCache.getCommentById(1L)).thenReturn(null);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        Comment result = commentService.getCommentById(1L);

        assertNull(result);
        verify(commentCache).getCommentById(1L);
        verify(commentRepository).findById(1L);
    }

    @Test
    void shouldUpdateComment() {
        Comment updatedDetails = mock(Comment.class);
        when(updatedDetails.getAuthor()).thenReturn("Updated Author");
        when(updatedDetails.getText()).thenReturn("Updated Text");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.updateComment(1L, updatedDetails);

        assertNotNull(result);
        verify(comment).setAuthor("Updated Author");
        verify(comment).setText("Updated Text");
        verify(commentRepository).findById(1L);
        verify(commentRepository).save(comment);
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistentComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        Comment result = commentService.updateComment(1L, comment);

        assertNull(result);
        verify(commentRepository).findById(1L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        boolean result = commentService.deleteComment(1L);

        assertTrue(result);
        verify(commentRepository).findById(1L);
        verify(commentRepository).deleteById(1L);
        verify(commentCache).removeComment(1L);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = commentService.deleteComment(1L);

        assertFalse(result);
        verify(commentRepository).findById(1L);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldSearchCommentsFromCache() {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentCache.getCommentsByAuthor("test author")).thenReturn(comments);

        List<Comment> result = commentService.searchComments(1L, "test author");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
        verify(commentCache).getCommentsByAuthor("test author");
        verify(commentRepository, never()).findByArticleIdAndAuthorContaining(anyLong(), anyString());
    }

    @Test
    void shouldSearchCommentsFromRepository() {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentCache.getCommentsByAuthor("test author")).thenReturn(Collections.emptyList());
        when(commentRepository.findByArticleIdAndAuthorContaining(1L, "test author")).thenReturn(comments);

        List<Comment> result = commentService.searchComments(1L, "test author");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Comment", result.get(0).getText());
        verify(commentCache).getCommentsByAuthor("test author");
        verify(commentRepository).findByArticleIdAndAuthorContaining(1L, "test author");
        verify(commentCache).putComment(comment);
    }

    @Test
    void shouldCreateCommentViaController() throws Exception {
        when(commentService.createComment(any(Comment.class))).thenReturn(comment);

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test Comment"));

        verify(commentService).createComment(any(Comment.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidCommentDataViaController() throws Exception {
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed: {author=Author cannot be empty, text=Text cannot be empty}"));

        verify(commentService, never()).createComment(any());
    }

    @Test
    void shouldGetAllCommentsViaController() throws Exception {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentService.getAllComments()).thenReturn(comments);

        mockMvc.perform(get("/api/comments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Test Comment"));

        verify(commentService).getAllComments();
    }

    @Test
    void shouldGetCommentByIdViaController() throws Exception {
        when(commentService.getCommentById(1L)).thenReturn(comment);

        mockMvc.perform(get("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test Comment"));

        verify(commentService).getCommentById(1L);
    }

    @Test
    void shouldReturnNotFoundForNonExistentCommentByIdViaController() throws Exception {
        when(commentService.getCommentById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(commentService).getCommentById(1L);
    }

    @Test
    void shouldUpdateCommentViaController() throws Exception {
        when(commentService.updateComment(anyLong(), any(Comment.class))).thenReturn(comment);

        mockMvc.perform(put("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test Comment"));

        verify(commentService).updateComment(eq(1L), any(Comment.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentCommentViaController() throws Exception {
        when(commentService.updateComment(anyLong(), any(Comment.class))).thenReturn(null);

        mockMvc.perform(put("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());

        verify(commentService).updateComment(eq(1L), any(Comment.class));
    }

    @Test
    void shouldDeleteCommentViaController() throws Exception {
        when(commentService.deleteComment(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCommentViaController() throws Exception {
        when(commentService.deleteComment(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(commentService).deleteComment(1L);
    }

    @Test
    void shouldSearchCommentsWithAuthorFilterViaController() throws Exception {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentService.searchComments(anyLong(), anyString())).thenReturn(comments);

        mockMvc.perform(get("/api/comments/search")
                .param("articleId", "1")
                .param("authorFilter", "Test Author")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Test Comment"));

        verify(commentService).searchComments(eq(1L), eq("Test Author"));
    }

    @Test
    void shouldSearchCommentsWithoutAuthorFilterViaController() throws Exception {
        List<Comment> comments = Collections.singletonList(comment);
        when(commentService.searchComments(anyLong(), anyString())).thenReturn(comments);

        mockMvc.perform(get("/api/comments/search")
                .param("articleId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Test Comment"));

        verify(commentService).searchComments(eq(1L), eq(""));
    }
}