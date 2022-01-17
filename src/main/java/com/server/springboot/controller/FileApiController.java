package com.server.springboot.controller;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.service.FileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.Resource;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class FileApiController {

    private final FileService fileService;

    @Autowired
    public FileApiController(FileService fileService) {
        this.fileService = fileService;
    }

    @ApiOperation(value = "Get image by id")
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable(value = "id") String imageId) {
        Image image = fileService.findImageById(imageId);
        Resource file = fileService.loadImage(image.getImageId(), image.getFilename());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"");
        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }
}
