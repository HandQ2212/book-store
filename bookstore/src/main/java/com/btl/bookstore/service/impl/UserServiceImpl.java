package com.btl.bookstore.service.impl;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.btl.bookstore.model.UserDtls;
import com.btl.bookstore.repository.UserRepository;
import com.btl.bookstore.service.CloudinaryService;
import com.btl.bookstore.service.UserService;
import com.btl.bookstore.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDtls> getUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {

        Optional<UserDtls> findByUser = userRepository.findById(id);

        if (findByUser.isPresent()) {
            UserDtls userDtls = findByUser.get();
            userDtls.setIsEnable(status);
            userRepository.save(userDtls);
            return true;
        }

        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(UserDtls user) {

        long lockTime = user.getLockTime().getTime();
        long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

        long currentTime = System.currentTimeMillis();

        if (unLockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        UserDtls findByEmail = userRepository.findByEmail(email.trim());
        findByEmail.setResetToken(resetToken);
        userRepository.save(findByEmail);
    }

    @Override
    public UserDtls getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public UserDtls updateUser(UserDtls user) {
        return userRepository.save(user);
    }

    @Override
    public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {

        UserDtls dbUser = userRepository.findById(user.getId()).get();

        try {
            if (img != null && !img.isEmpty()) {
                // Delete old image from Cloudinary
                String oldImage = dbUser.getProfileImage();
                if (oldImage != null && !oldImage.isEmpty() && oldImage.startsWith("https")) {
                    try {
                        String publicId = extractPublicIdFromUrl(oldImage, "profile");
                        if (publicId != null) {
                            cloudinaryService.deleteImage(publicId);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to delete old profile image", e);
                    }
                }
                // Upload new image
                String imageUrl = cloudinaryService.uploadImage(img, "profile");
                dbUser.setProfileImage(imageUrl);
            }

            if (!ObjectUtils.isEmpty(dbUser)) {
                dbUser.setName(user.getName());
                dbUser.setMobileNumber(user.getMobileNumber());
                dbUser.setAddress(user.getAddress());
                dbUser.setCity(user.getCity());
                dbUser.setState(user.getState());
                dbUser.setPincode(user.getPincode());
                dbUser = userRepository.save(dbUser);
            }
        } catch (Exception e) {
            logger.error("Error uploading profile image", e);
        }

        return dbUser;
    }

    @Override
    public UserDtls saveAdmin(UserDtls user) {
        user.setRole("ROLE_ADMIN");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private String extractPublicIdFromUrl(String url, String folder) {
        try {
            if (url == null || !url.contains("cloudinary")) {
                return null;
            }
            // URL format: https://res.cloudinary.com/da4dr8ghb/image/upload/v1234567890/bookstore/profile/public_id.ext
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex == -1) return null;

            String fileNameWithExt = url.substring(lastSlashIndex + 1);
            int dotIndex = fileNameWithExt.lastIndexOf('.');
            if (dotIndex > 0) {
                return folder + "/" + fileNameWithExt.substring(0, dotIndex);
            }
            return folder + "/" + fileNameWithExt;
        } catch (Exception e) {
            logger.warn("Failed to extract public ID from URL: " + url, e);
            return null;
        }
    }

}
