package com.server.springboot.service;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;

public interface FileService {

    void saveImage(Image image, MultipartFile file);

    void deleteImage(String imageId) throws IOException;

    Resource loadImage(String imageId, String filename);

    Set<Image> storageImages(List<MultipartFile> imageFiles, User creator);

    Image storageOneImage(MultipartFile imageFile, User creator, boolean assignToProfile);

    Image findImageById(String id);

}
