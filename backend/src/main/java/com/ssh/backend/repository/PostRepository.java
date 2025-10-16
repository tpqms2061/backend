package com.ssh.backend.repository;

import com.ssh.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 작성자 정보를 함께 조회 (N+1 문제 방지)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUser(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId ")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithUserAndLikes(@Param("postId") Long postId);


    /**
     * 특정 사용자의 게시물 조회 (페이징)
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.user.id = :userId " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

}

