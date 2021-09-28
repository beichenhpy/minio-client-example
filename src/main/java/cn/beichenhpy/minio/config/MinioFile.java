package cn.beichenhpy.minio.config;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author beichenhpy
 * @version 1.0.0
 * @apiNote
 * @since 2021/9/27 22:03
 */
@Builder
@Data
public class MinioFile implements Serializable {
    private static final long serialVersionUID = -7603375354898490241L;
    //origin filename
    private String originName;
    //url
    private String url;
}
