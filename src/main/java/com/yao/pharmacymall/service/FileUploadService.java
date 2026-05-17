package com.yao.pharmacymall.service;

import com.yao.pharmacymall.config.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件上传服务
 * 支持处方图片、资质文件等上传
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.url:/files}")
    private String accessUrl;

    @Value("${file.max-size:10485760}") // 默认10MB
    private long maxFileSize;

    /**
     * 允许的文件类型
     */
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    ));

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = new HashSet<>(Arrays.asList(
        "application/pdf", "image/jpeg", "image/png"
    ));

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @return 访问URL
     */
    public String uploadFile(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES);

        String fileName = generateFileName(file.getOriginalFilename());
        String subDir = generateSubDir();
        String fullPath = uploadPath + File.separator + subDir;

        try {
            // 创建目录
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(fullPath, fileName);
            file.transferTo(filePath.toFile());

            // 返回访问URL
            return accessUrl + "/" + subDir + "/" + fileName;

        } catch (IOException e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @return 访问URL列表
     */
    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(uploadFile(file));
            }
        }
        return urls;
    }

    /**
     * 上传处方图片（仅允许图片格式）
     *
     * @param file 文件
     * @return 访问URL
     */
    public String uploadPrescriptionImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES);
        return uploadFile(file);
    }

    /**
     * 上传资质文件（允许图片和PDF）
     *
     * @param file 文件
     * @return 访问URL
     */
    public String uploadQualificationFile(MultipartFile file) {
        validateFile(file, ALLOWED_DOCUMENT_TYPES);
        return uploadFile(file);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, Set<String> allowedTypes) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(String.format("文件大小不能超过%sMB", maxFileSize / 1024 / 1024));
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }

        // 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 生成子目录（按日期）
     */
    private String generateSubDir() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%d/%02d/%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
}
