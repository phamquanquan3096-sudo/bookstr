package com.quanly.controller;

import com.quanly.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @PostMapping
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error("File tải lên không hợp lệ!");
        }

        try {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            } else {
                ext = ".jpg";
            }

            String fileName = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + ext;
            File destFile = new File(uploadDir, fileName);

            try (InputStream is = file.getInputStream();
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            String fileUrl = "/uploads/" + fileName;
            return ApiResponse.ok("Tải ảnh lên thành công", fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi khi lưu tệp ảnh: " + e.getMessage());
        }
    }
}
