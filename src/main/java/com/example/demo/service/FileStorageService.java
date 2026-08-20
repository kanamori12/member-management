package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";

    public String saveProfileImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "ファイル名を取得できません。");
        }

        String extension = getExtension(originalFilename);

        String savedFilename = UUID.randomUUID().toString()
                + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR);

        try {

            Files.createDirectories(uploadPath);

            Path destination = uploadPath.resolve(savedFilename);

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "画像ファイルの保存に失敗しました。",
                    e);
        }

        return savedFilename;
    }

    private String getExtension(String filename) {

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1) {
            return "";
        }

        return filename.substring(dotIndex);
    }

    public void deleteProfileImage(String filename) {

        if (filename == null || filename.isBlank()) {
            return;
        }

        Path filePath = Paths.get(UPLOAD_DIR)
                .resolve(filename);

        try {

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "画像ファイルの削除に失敗しました。",
                    e);
        }
    }
}