package cn.beichenhpy.minio.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author beichenhpy
 * @version 1.0.0
 * @apiNote
 * @since 2021/9/27 21:35
 */
@Configuration
@EnableConfigurationProperties(MinioProp.class)
public class MinioConfig {

    public static final String DEFAULT_BUCKET = "hpy";

    @Resource
    private MinioProp minioProp;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioProp.getEndpoint())
                .credentials(minioProp.getAccessKey(),minioProp.getSecretKey())
                .build();
    }

}
