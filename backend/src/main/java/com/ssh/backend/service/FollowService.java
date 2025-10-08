package com.ssh.backend.service;

import com.ssh.backend.dto.FollowResponse;
import com.ssh.backend.dto.UserResponse;
import com.ssh.backend.entity.Follow;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.BadRequestException;
import com.ssh.backend.exception.ResourceNotFoundException;
import com.ssh.backend.repository.FollowRepository;
import com.ssh.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    //팔로우 토글

    public FollowResponse toggleFollow(Long userId) {

       User currentUser =  authenticationService.getCurrentUser();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(()->  new ResourceNotFoundException("대상 사용자를 찾을 수 없습니다"));

        // 자기 자신 팔로우 방지
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new BadRequestException("자기 자신을 팔로우할 수 없습니다");
        }

        //이미 팔로우했는지 확인
        boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);

        if (isFollowing) {
            followRepository.deleteByFollowerAndFollowing(currentUser, targetUser);
            isFollowing= false;
        }else {
            Follow follow = Follow.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build();
            followRepository.save(follow);
            isFollowing = true;
        }
        Long followersCount = followRepository.countFollowers(targetUser);
        Long followingCount = followRepository.countFollowing(targetUser);

        return FollowResponse.builder()
                .isFollowing(isFollowing)
                .followerCount(followersCount)
                .followingCount(followingCount)
                .build();
    }
    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<User> followers = followRepository.findFollowers(user, pageable);
        User currentUser = authenticationService.getCurrentUser();

        return followers.map(follower -> {
            boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, follower);
            return mapToUserResponse(follower, isFollowing);
        });

    }
    /**
     * 팔로잉 목록 조회
     */

    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<User> following = followRepository.findFollowing(user, pageable);
        User currentUser = authenticationService.getCurrentUser();

        return following.map(followedUser -> {
            boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, followedUser);
            return mapToUserResponse(followedUser, isFollowing);
        });

    }



    private UserResponse mapToUserResponse(User user, boolean isFollowing) {
        Long followersCount = followRepository.countFollowers(user);
        Long followingCount = followRepository.countFollowing(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }
}
