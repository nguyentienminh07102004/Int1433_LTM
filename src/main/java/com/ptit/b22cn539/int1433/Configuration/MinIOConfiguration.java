package com.ptit.b22cn539.int1433.Configuration;

import io.minio.MinioClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinIOConfiguration {
    @Value(value = "${minio.host}")
    String host;
    @Value(value = "${minio.access_key}")
    String accessKey;
    @Value(value = "${minio.secret_key}")
    String secretKey;

    @Bean
    MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(this.host)
                .credentials(this.accessKey, this.secretKey)
                .build();
    }
}