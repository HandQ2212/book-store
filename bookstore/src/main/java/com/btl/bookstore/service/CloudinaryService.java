package com.btl.bookstore.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folder) throws Exception;
    void deleteImage(String publicId) throws Exception;
}