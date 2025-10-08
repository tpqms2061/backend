package com.ssh.backend.service;

import com.ssh.backend.dto.PostRequest;
import com.ssh.backend.dto.PostResponse;
import com.ssh.backend.entity.Post;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.ResourceNotFoundException;
import com.ssh.backend.exception.UnauthorizedException;
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
        authenticationService.getCurrentUser();
        Page<Post> posts = postRepository.findAllWithUser(pageable);
        return posts.map(PostResponse::from);
    }

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

//    //게시글 조회
//    @Transactional(readOnly = true)
//    public PostResponse getPost(Long postId) {
//        authenticationService.getCurrentUser();
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
//
//        return PostResponse.from(post);
//    }
}
