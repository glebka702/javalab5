package com.example.javalabs2.Repository;

import com.example.javalabs2.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleId(Long articleId);
    @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND LOWER(c.author) LIKE LOWER(CONCAT('%', :authorFilter, '%'))")
    List<Comment> findByArticleIdAndAuthorContaining(@Param("articleId") Long articleId,
                                                     @Param("authorFilter") String authorFilter);
}
