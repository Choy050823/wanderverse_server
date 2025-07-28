package com.backend.wanderverse_server.service.impl;

import com.backend.wanderverse_server.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Override
    public String saveFile(MultipartFile multipartFile) throws IOException {
        try {
            // convert into multipart file(image data) and add into the bucket
//            File file = convertMultiPartFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            Map<String, String> metaData = new HashMap<>();
            metaData.put("Content-Type", multipartFile.getContentType());

            // set all added files as public (direct access to image in S3)
            // change to secured temp access with this code later: (predesigned url)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(multipartFile.getContentType())
                            .metadata(metaData)
//                            .acl("public-read")     // should set to predesigned url later in production
                            .build();

            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(multipartFile.getBytes())
            );

            return generateFileUrl(fileName);
        } catch (Exception e) {
            throw new IOException("Error uploading file: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getFile(String filename) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            throw new IOException("Error downloading file: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateFileUrl(String fileName) {
        if (!endpoint.isEmpty()) {
            // MinIO URL
            return String.format("%s/%s/%s", endpoint, bucketName, fileName);
        } else {
            // S3 URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        }
    }
}
