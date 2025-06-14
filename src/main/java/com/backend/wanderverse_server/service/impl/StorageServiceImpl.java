package com.backend.wanderverse_server.service.impl;

//import com.amazonaws.HttpMethod;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.CannedAccessControlList;
//import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.amazonaws.services.s3.model.S3Object;
import com.backend.wanderverse_server.service.StorageService;
import org.apache.commons.io.IOExceptionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
            // convert into multipart file(image) and add into the bucket
//            File file = convertMultiPartFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            Map<String, String> metaData = new HashMap<>();
            metaData.put("Content-Type", multipartFile.getContentType());

            // set all added files as public (direct access to image in S3)
            // change to secured temp access with this code later:
//            amazonS3.generatePresignedUrl(new GeneratePresignedUrlRequest(bucketName, fileName)
//                    .withMethod(HttpMethod.GET)
//                    .withExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))); // 1-hour expiration

//            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file)
//                    .withCannedAcl(CannedAccessControlList.PublicRead));
//
//            file.delete();

            software.amazon.awssdk.services.s3.model.PutObjectRequest putObjectRequest =
                    software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
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
//            S3Object s3Object = amazonS3.getObject(bucketName, filename);
//            return s3Object.getObjectContent().readAllBytes();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            throw new IOException("Error downloading file: " + e.getMessage(), e);
        }
    }

//    private File convertMultiPartFile(MultipartFile file) throws IOException {
//        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
//        try (FileOutputStream fos = new FileOutputStream(convFile)) {
//            fos.write(file.getBytes());
//        }
//        return convFile;
//    }

    private String generateFileUrl(String fileName) {
        if (!endpoint.isEmpty()) {
            // MinIO URL
            return String.format("%s/%s/%s", endpoint, bucketName, fileName);
        } else {
            // S3 URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        }
    }
}
