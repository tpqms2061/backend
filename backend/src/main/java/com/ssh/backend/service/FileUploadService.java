package com.ssh.backend.service;

import com.ssh.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 파일 업로드 서비스
 */
@Service
public class FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;  // application.yml에서 설정: ./uploads/profiles

    /**
     * 프로필 이미지 업로드
     *
     * @return 접근 가능한 이미지 URL
     */
    public String uploadProfileImage(MultipartFile file) throws IOException {
        // 1. 파일 검증
        if (file.isEmpty()) {
            throw new org.apache.coyote.BadRequestException("파일이 비어있습니다");
        }

        // 2. 파일 타입 검증 (이미지만 허용)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("이미지 파일만 업로드 가능합니다");
        }

        // 3. 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 4. 고유한 파일명 생성 (UUID + 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 5. 저장 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 6. 파일 저장
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 7. 접근 가능한 URL 반환
        return "/api/upload/profiles/" + storedFilename;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // URL에서 파일명 추출
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir).resolve(filename);

        // 파일 존재 시 삭제
        Files.deleteIfExists(filePath);
    }
}