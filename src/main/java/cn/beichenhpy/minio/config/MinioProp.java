package cn.beichenhpy.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author beichenhpy
 * @version 1.0.0
 * @apiNote
 * @since 2021/9/27 21:42
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    /**
     * 连接地址
     */
    private String endpoint;
    /**
     * 用户名
     */
    private String accessKey;
    /**
     * 密码
     */
    private String secretKey;
}
