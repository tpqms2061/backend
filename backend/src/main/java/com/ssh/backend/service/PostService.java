package com.ssh.backend.service;

import com.ssh.backend.dto.PostRequest;
import com.ssh.backend.dto.PostResponse;
import com.ssh.backend.entity.Post;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.ResourceNotFoundException;
import com.ssh.backend.exception.UnauthorizedException;
import com.ssh.backend.repository.CommentRepository;
import com.ssh.backend.repository.LikeRepository;
import com.ssh.backend.repository.PostRepository;
import com.ssh.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    //생성
    public PostResponse createPost(PostRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        Post post = Post.builder()
                .content(request.getContent())
                .user(currentUser)

                .build();

        Post savedPost = postRepository.save(post);

        return PostResponse.from(savedPost);

    }

    //게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        Page<Post> posts = postRepository.findAllWithUser(pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.from(post);
            Long likeCount = likeRepository.countByPostId(post.getId());
            boolean isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            Long commentCount = commentRepository.countByPostId(post.getId());

            response.setLikeCount(likeCount);
            response.setLiked(isLiked);
            response.setCommentCount(commentCount);

            return response;
        });
    }

    //

    //삭제
    public void deletePost(Long postId) {
        User currentUser = authenticationService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new ResourceNotFoundException("Post not found"));

        // 권한 확인
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("게시물을 삭제할 권한이 없습니다");
        }

        postRepository.delete(post);
    }

    //수정

    public PostResponse updatePost(Long postId, PostRequest request) {

        User currentUser = authenticationService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setContent(request.getContent());
        Post updatedPost= postRepository.save(post);
        return PostResponse.from(updatedPost);
    }

    //단일 게시글 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findByIdWithUserAndLikes(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다"));


        return PostResponse.from(post);
    }

    //게시글 갯수
    @Transactional(readOnly = true)
    public Long getUserPostCount(Long userId) {
        authenticationService.getCurrentUser();
        return postRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.from(post);
            Long likeCount = likeRepository.countByPostId(post.getId());
            boolean isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            Long commentCount = commentRepository.countByPostId(post.getId());

            response.setLikeCount(likeCount);
            response.setLiked(isLiked);
            response.setCommentCount(commentCount);


            return response;
        });
    }
}
