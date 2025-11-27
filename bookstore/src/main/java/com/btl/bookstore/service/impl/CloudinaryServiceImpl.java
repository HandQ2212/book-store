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

    @Override
    public String uploadImage(MultipartFile file, String folder) throws Exception {
        try {
            logger.info("Uploading image: {} to folder: {}", file.getOriginalFilename(), folder);
            
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
            
            logger.info("Image uploaded successfully. URL: {}, PublicId: {}", imageUrl, publicId);
            return imageUrl;
        } catch (IOException e) {
            logger.error("Error uploading image to Cloudinary", e);
            throw new Exception("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    public void deleteImage(String publicId) throws Exception {
        try {
            if (publicId == null || publicId.isEmpty()) {
                return;
            }
            logger.info("Deleting image: {}", publicId);
            getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Image deleted successfully");
        } catch (IOException e) {
            logger.error("Error deleting image from Cloudinary", e);
            throw new Exception("Failed to delete image from Cloudinary", e);
        }
    }
}