package com.ssh.backend.controller;

import com.ssh.backend.dto.UpdateProfileRequest;
import com.ssh.backend.dto.UserResponse;
import com.ssh.backend.entity.User;
import com.ssh.backend.service.FileUploadService;
import com.ssh.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            User user
    ) {
        UserResponse response = userService.updateProfile(request, user.getUsername());
        return ResponseEntity.ok(response);
    }
}