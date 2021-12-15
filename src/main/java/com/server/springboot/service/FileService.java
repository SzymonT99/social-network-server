package com.server.springboot.service;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface FileService {

    Set<Image> storageImages(List<MultipartFile> imageFiles, User creator);

    Image storageOneImage(MultipartFile imageFile, User creator, boolean assignToProfile);

    Image findImageById(String id);

}
