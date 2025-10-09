package com.ssh.backend.dto;

import com.ssh.backend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String content;
    private String username;
//    private String imageUrl;
    private UserDto user;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private  boolean isLiked;
    private Long commentCount;


    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .user(UserDto.fromEntity(post.getUser()))
                .createAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

    }
}
