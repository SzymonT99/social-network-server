package com.server.springboot.service.impl;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.entity.UserProfile;
import com.server.springboot.domain.repository.ImageRepository;
import com.server.springboot.domain.repository.UserRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Autowired
    public FileServiceImpl(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<Image> storageImages(List<MultipartFile> imageFiles, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserProfile userProfile = user.getUserProfile();
        System.out.println("userProfile :" + userProfile);

        Set<Image> postImages = new HashSet<>();

        imageFiles.forEach(file -> {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            try {
                if (!file.isEmpty()) {
                    Image newImage = Image.builder()
                            .filename(fileName)
                            .data(file.getBytes())
                            .type(file.getContentType())
                            .addedIn(LocalDateTime.now())
                            .isProfilePhoto(false)
                            .userProfile(userProfile)
                            .build();
                    imageRepository.save(newImage);
                    postImages.add(newImage);
                }
            } catch (IOException e) {
                LOGGER.error("Uploading file error - message: {}", e.getMessage());
                e.printStackTrace();
            }
        });
        return postImages;
    }

    @Override
    public Image findImageById(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found image with id: " + id));
    }
}