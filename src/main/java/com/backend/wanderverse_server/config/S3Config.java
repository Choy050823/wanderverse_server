package com.backend.wanderverse_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${AWS_ACCESS_KEY_ID:${aws.accessKeyId:}}")
    private String accessKey;

    @Value("${AWS_SECRET_ACCESS_KEY:${aws.secretKey:}}")
    private String secretKey;

    @Value("${AWS_REGION:${aws.region:}}")
    private String region;

    @Value("${AWS_S3_ENDPOINT:${aws.s3.endpoint:}}")
    private String endpoint;

    @Value("${AWS_S3_BUCKET:${aws.s3.bucket:}}")
    private String bucket;

    @Bean
    public S3Client s3Client() {
        if (accessKey.isEmpty() || secretKey.isEmpty() || region.isEmpty()) {
            throw new IllegalStateException("AWS credentials and region must be provided");
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }

    @Bean
    public String s3BucketName() {
        return bucket;
    }
}