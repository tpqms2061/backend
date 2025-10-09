//package com.ssh.backend.controller;
//
//import com.ssh.backend.dto.UserResponse;
//import com.ssh.backend.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<UserResponse> getUserByUser(@PathVariable Long userId) {
//        UserResponse user = userService.getUserByUsername(username);
//        return ResponseEntity.ok(user);
//    }
//}