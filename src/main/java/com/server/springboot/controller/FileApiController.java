package com.server.springboot.controller;

import com.server.springboot.domain.entity.Image;
import com.server.springboot.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class FileApiController {

    private final FileService fileService;

    @Autowired
    public FileApiController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(value = "/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable(value = "id") String imageId) {
        Image image = fileService.findImageById(imageId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"");
        return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
    }
}
