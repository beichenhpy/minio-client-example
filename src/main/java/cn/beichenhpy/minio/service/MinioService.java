package cn.beichenhpy.minio.service;

import cn.beichenhpy.minio.config.MinioFile;
import cn.beichenhpy.minio.config.MinioProp;
import io.minio.*;
import io.minio.messages.Bucket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author beichenhpy
 * @version 1.0.0
 * @apiNote
 * @since 2021/9/27 21:45
 */
@Slf4j
@Service
public class MinioService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProp minioProp;

    /**
     * 创建bucket
     */
    public void createBucket(String bucketName) throws Exception {
        if (!minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName).build()
        )) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName).build()
            );
        }
    }
    //创建空文件夹
    @SneakyThrows
    public void createEmptyFolder(String bucketName, String folderName){
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                        new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());
    }

    /**
     * 上传文件
     */
    public ResponseEntity<MinioFile> uploadFile(MultipartFile file, String bucketName) throws Exception {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            return null;
        }
        //判断存储桶是否存在  不存在则创建
        createBucket(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //开始上传
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(originalFilename)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1)
                        .contentType(file.getContentType()).build()
        );
        String url = minioProp.getEndpoint() + File.separator + bucketName + File.separator + originalFilename;
        log.info("上传文件成功url ：[{}]", url);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(
                        MinioFile.builder()
                                .originName(originalFilename)
                                .url(url).build()
                );
    }

    /**
     * 获取全部bucket
     *
     * @return buckets
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public Optional<Bucket> getBucket(String bucketName) {
        return minioClient.listBuckets()
                .stream()
                .filter(b -> b.name().equals(bucketName))
                .findFirst();
    }

    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        minioClient.removeBucket(
                RemoveBucketArgs.builder()
                        .bucket(bucketName).build()
        );
    }

    /**
     * 获取⽂件外链
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param expires    过期时间 <=7
     * @return url
     */
    public String getObjectURL(String bucketName, String objectName, Integer expires) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires).build()
        );
    }

    /**
     * 获取⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @return ⼆进制流
     */
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName).build()
        );
    }

    /**
     * 上传⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param stream     ⽂件流
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream) throws
            Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream,
                                stream.available(),
                                -1)
                        .contentType(objectName.substring(objectName.lastIndexOf("."))).build()
        );
    }

    /**
     * 上传⽂件
     *
     * @param bucketName  bucket名称
     * @param objectName  ⽂件名称
     * @param stream      ⽂件流
     * @param size        ⼤⼩
     * @param contextType 类型
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream, long
            size, String contextType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream,
                                size,
                                -1)
                        .contentType(contextType).build()
        );
    }

    /**
     * 获取⽂件信息
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucketName, String objectName) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName).build()
        );
    }

    /**
     * 删除⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-apireference.html#removeObject
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName).build()
        );
    }
}
