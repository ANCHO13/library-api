package com.example.library.service;

import com.example.library.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded files on the local disk.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf");

    private final Path uploadRoot;
    private final String uploadDirName;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDirName = uploadDir;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory: " + this.uploadRoot, e);
        }
    }

    /**
     * Saves the file under a generated name and returns its relative path,
     * e.g. "uploads/7c1e...-.png". That path is what gets stored in the database.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Uploaded file is empty");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("File type not allowed. Allowed: " + ALLOWED_EXTENSIONS);
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new FileStorageException("Invalid file path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Could not store file", e);
        }
        return uploadDirName + "/" + storedName;
    }

    /**
     * Deletes a previously stored file. Missing files are ignored.
     */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        String fileName = Paths.get(relativePath).getFileName().toString();
        try {
            Files.deleteIfExists(uploadRoot.resolve(fileName).normalize());
        } catch (IOException ignored) {
            // A leftover file on disk is not worth failing the request for.
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }
}
