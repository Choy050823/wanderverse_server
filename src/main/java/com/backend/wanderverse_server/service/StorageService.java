package com.backend.wanderverse_server.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String saveFile(MultipartFile file) throws IOException;
    byte[] getFile(String filename) throws IOException;
}
