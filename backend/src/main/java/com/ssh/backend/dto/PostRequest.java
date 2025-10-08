package com.ssh.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 2200, message = "Content must not exceed 2200 characters")
    private String content;

//    private String imageUrl;




}
