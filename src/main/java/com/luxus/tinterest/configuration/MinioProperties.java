package com.luxus.tinterest.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {

    private String endpoint;

    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    private String bucket;
}
