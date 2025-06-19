package com.example.javalabs2.Service;

import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Repository.CommentRepository;
import com.example.javalabs2.Cache.CommentCache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentCache commentCache;

    public CommentService(CommentRepository commentRepository,
                          CommentCache commentCache) {
        this.commentRepository = commentRepository;
        this.commentCache = commentCache;
    }

    public Comment createComment(Comment comment) {
        Comment saved = commentRepository.save(comment);
        commentCache.putComment(saved);
        return saved;
    }

    public List<Comment> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        comments.forEach(commentCache::putComment);
        return comments;
    }

    public Comment getCommentById(Long id) {
        Comment cached = commentCache.getCommentById(id);
        if (cached != null) {
            return cached;
        }
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            Comment found = comment.get();
            commentCache.putComment(found);
            return found;
        }
        return null;
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return null;
        }
        Comment comment = commentOpt.get();
        comment.setAuthor(commentDetails.getAuthor());
        comment.setText(commentDetails.getText());
        Comment updated = commentRepository.save(comment);
        commentCache.putComment(updated);
        return updated;
    }

    public boolean deleteComment(Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return false;
        }
        commentRepository.deleteById(id);
        commentCache.removeComment(id);
        return true;
    }

    public List<Comment> searchComments(Long articleId, String authorFilter) {
        List<Comment> cached = commentCache.getCommentsByAuthor(authorFilter);
        if (!cached.isEmpty() && cached.stream().allMatch(
                c -> c.getArticle() != null && c.getArticle().getId().equals(articleId))) {
            return cached;
        }
        List<Comment> comments = commentRepository.findByArticleIdAndAuthorContaining(articleId, authorFilter);
        comments.forEach(commentCache::putComment);
        return comments;
    }
}