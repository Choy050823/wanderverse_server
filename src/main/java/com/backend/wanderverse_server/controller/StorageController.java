package com.backend.wanderverse_server.controller;

import com.backend.wanderverse_server.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("image") MultipartFile multipartFile) throws IOException {
        String url = storageService.saveFile(multipartFile);
        Map<String, String> response = new HashMap<>();
        // set the imageUrl and return back
        response.put("imageUrl", url);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileName) throws IOException{
        try {
            byte[] data = storageService.getFile(fileName);
            if (data == null || data.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Content-Disposition", "attachment; filename=" + fileName)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
