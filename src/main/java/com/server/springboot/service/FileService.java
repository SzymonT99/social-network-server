package com.server.springboot.service;

import com.server.springboot.domain.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface FileService {

    Set<Image> storageImages(List<MultipartFile> imageFiles, Long userId);

    Image findImageById(String id);

}
