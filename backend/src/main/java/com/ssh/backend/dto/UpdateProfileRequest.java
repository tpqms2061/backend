package com.ssh.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {


    private String fullName;


    private String bio;


    private String profileImageUrl;
}