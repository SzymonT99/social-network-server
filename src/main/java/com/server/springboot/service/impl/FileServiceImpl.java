package com.server.springboot.service.impl;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.entity.UserProfile;
import com.server.springboot.domain.repository.ImageRepository;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {

    private final ImageRepository imageRepository;
    private final Path rootFolder = Paths.get("uploads");

    @Autowired
    public FileServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void saveImage(Image image, MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.rootFolder.resolve(image.getImageId() + "_" + file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Error with save image: " + e.getMessage());
        }
    }

    @Override
    public Resource loadImage(String imageId, String filename) {
        try {
            Path file = rootFolder.resolve(imageId + "_" + filename);
            Resource resourceFile = new UrlResource(file.toUri());
            if (resourceFile.exists() || resourceFile.isReadable()) {
                return resourceFile;
            } else {
                throw new RuntimeException("Error with resource file");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error with Url resource creation: " + e.getMessage());
        }
    }

    @Override
    public Set<Image> storageImages(List<MultipartFile> imageFiles, User creator) {
        UserProfile userProfile = creator.getUserProfile();

        Set<Image> postImages = new HashSet<>();

        imageFiles.forEach(file -> {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            if (!file.isEmpty()) {
                Image image = Image.builder()
                        .filename(fileName)
                        .type(file.getContentType())
                        .filePath("/uploads/" + fileName)
                        .addedIn(LocalDateTime.now())
                        .userProfile(userProfile)
                        .build();
                imageRepository.save(image);
                postImages.add(image);
                saveImage(image, file);
            }
        });
        return postImages;
    }

    @Override
    public Image storageOneImage(MultipartFile imageFile, User creator, boolean assignToProfile) {
        UserProfile userProfile = creator.getUserProfile();

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));
        Image image = null;
        if (!imageFile.isEmpty()) {
            image = Image.builder()
                    .filename(fileName)
                    .filePath("/uploads/" + fileName)
                    .type(imageFile.getContentType())
                    .addedIn(LocalDateTime.now())
                    .build();

            if (assignToProfile) {
                image.setUserProfile(userProfile);
            }

            imageRepository.save(image);
            saveImage(image, imageFile);
        }
        return image;
    }

    @Override
    public Image findImageById(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found image with id: " + id));
    }
}
