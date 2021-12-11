package com.server.springboot.service;

import com.server.springboot.domain.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    List<Image> storeImages(List<MultipartFile> files, Long userId);

    Image findImageById(String id);

}
