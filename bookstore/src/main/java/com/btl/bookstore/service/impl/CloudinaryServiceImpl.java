package com.btl.bookstore.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.btl.bookstore.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/*
Service tích hợp Cloudinary để quản lý ảnh
Tải lên và xóa ảnh trên Cloudinary
*/
@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    // Lấy instance Cloudinary (Lazy init)
    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
        }
        return cloudinary;
    }

    // Tải ảnh lên Cloudinary
    @Override
    public String uploadImage(MultipartFile file, String folder) throws Exception {
        try {
            logger.info("Tải ảnh lên: {} vào thư mục: {}", file.getOriginalFilename(), folder);
            
            Map uploadResult = getCloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookstore/" + folder,
                            "resource_type", "auto",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            logger.info("Ảnh đã tải lên thành công. URL: {}, PublicId: {}", imageUrl, publicId);
            return imageUrl;
        } catch (IOException e) {
            logger.error("Lỗi khi tải ảnh lên Cloudinary", e);
            throw new Exception("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    // Xóa ảnh khỏi Cloudinary
    @Override
    public void deleteImage(String publicId) throws Exception {
        try {
            if (publicId == null || publicId.isEmpty()) {
                return;
            }
            logger.info("Xóa ảnh: {}", publicId);
            getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Ảnh đã được xóa thành công");
        } catch (IOException e) {
            logger.error("Lỗi khi xóa ảnh từ Cloudinary", e);
            throw new Exception("Không thể xóa ảnh từ Cloudinary", e);
        }
    }
}