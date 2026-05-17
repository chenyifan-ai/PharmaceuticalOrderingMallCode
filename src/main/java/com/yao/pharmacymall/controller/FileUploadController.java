package com.yao.pharmacymall.controller;

import com.yao.pharmacymall.common.Result;
import com.yao.pharmacymall.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 单文件上传（通用）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("filename", file.getOriginalFilename());
        return Result.success("上传成功", data);
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = fileUploadService.uploadFiles(files);
        return Result.success("上传成功", urls);
    }

    /**
     * 上传处方图片
     */
    @PostMapping("/upload/prescription")
    public Result<Map<String, String>> uploadPrescription(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadPrescriptionImage(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    /**
     * 上传资质文件
     */
    @PostMapping("/upload/qualification")
    public Result<Map<String, String>> uploadQualification(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadQualificationFile(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }
}
