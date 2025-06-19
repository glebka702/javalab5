package com.example.javalabs2.Cache;

import com.example.javalabs2.Entity.Comment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommentCache {
    private final Map<Long, Comment> commentByIdMap = new HashMap<>();
    private final Map<String, List<Comment>> commentsByAuthorMap = new HashMap<>();
    private final Map<Long, List<Comment>> commentsByArticleMap = new HashMap<>();

    public Comment getCommentById(Long id) {
        return commentByIdMap.get(id);
    }

    public List<Comment> getCommentsByAuthor(String author) {
        return commentsByAuthorMap.getOrDefault(author.toLowerCase(), new ArrayList<>());
    }

    public List<Comment> getCommentsByArticle(Long articleId) {
        return commentsByArticleMap.getOrDefault(articleId, new ArrayList<>());
    }

    public void putComment(Comment comment) {
        commentByIdMap.put(comment.getId(), comment);

        String authorKey = comment.getAuthor().toLowerCase();
        commentsByAuthorMap.computeIfAbsent(authorKey, k -> new ArrayList<>()).add(comment);

        if (comment.getArticle() != null) {
            Long articleId = comment.getArticle().getId();
            commentsByArticleMap.computeIfAbsent(articleId, k -> new ArrayList<>()).add(comment);
        }
    }

    public void removeComment(Long id) {
        Comment comment = commentByIdMap.remove(id);
        if (comment != null) {
            String authorKey = comment.getAuthor().toLowerCase();
            List<Comment> authorComments = commentsByAuthorMap.get(authorKey);
            if (authorComments != null) {
                authorComments.removeIf(c -> c.getId().equals(id));
            }

            if (comment.getArticle() != null) {
                Long articleId = comment.getArticle().getId();
                List<Comment> articleComments = commentsByArticleMap.get(articleId);
                if (articleComments != null) {
                    articleComments.removeIf(c -> c.getId().equals(id));
                }
            }
        }
    }
}