package cn.beichenhpy.minio.controller;

import cn.beichenhpy.minio.config.MinioConfig;
import cn.beichenhpy.minio.config.MinioFile;
import cn.beichenhpy.minio.service.MinioService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author beichenhpy
 * @version 1.0.0
 * @apiNote
 * @since 2021/9/27 22:09
 */
@Slf4j
@RestController
public class MinioController {

    @Resource
    private MinioService minioService;

    @PostMapping("/upload/{bucket}")
    @SneakyThrows
    public ResponseEntity<MinioFile> upload(MultipartFile file, @PathVariable("bucket") String bucket) {
        return minioService.uploadFile(file, bucket);
    }

    @PostMapping("/bucket")
    @SneakyThrows
    public ResponseEntity<?> createBucket(@RequestParam("bucket") String bucket) {
        minioService.createBucket(bucket);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/folder")
    @SneakyThrows
    public ResponseEntity<?> createFolder(@RequestParam("bucket") String bucket,
                                          @RequestParam("folder") String folder) {
        minioService.createEmptyFolder(bucket, folder);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/file/download")
    @SneakyThrows
    public void download(@RequestParam("bucket") String bucket,
                         @RequestParam("fileName") String fileName,
                         HttpServletResponse response) {
        //judge exist
        minioService.getObjectInfo(bucket, fileName);
        //get object
        try (InputStream inputStream = minioService.getObject(bucket, fileName)) {
            try (OutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 预览
     *
     * @param bucket   桶
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/file/{bucket}/preview/**")
    @SneakyThrows
    public void preview(
            @PathVariable("bucket") String bucket,
            HttpServletRequest request,
            HttpServletResponse response) {
        String path = request.getServletPath();
        String file = path.substring(path.lastIndexOf("preview"));
        file = file.substring(file.indexOf("/") + 1);
        try {
            minioService.getObjectInfo(bucket, file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        try (InputStream inputStream = minioService.getObject(bucket, file)) {
            try (OutputStream outputStream = response.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}