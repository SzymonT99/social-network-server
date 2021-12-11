package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestPostDto;
import com.server.springboot.domain.entity.Image;
import com.server.springboot.service.FileService;
import com.server.springboot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class PostApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final FileService fileService;
    private final PostService postService;

    @Autowired
    public PostApiController(FileService fileService, PostService postService) {
        this.fileService = fileService;
        this.postService = postService;
    }

    // tworzenie posta ze zdjeciami
    @PostMapping(value = "/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@RequestPart(value = "images") List<MultipartFile> images, @Valid @RequestPart(value = "post") RequestPostDto postDto) {
        LOGGER.info("---- Create post");
        List<Image> postImages = fileService.storeImages(images, postDto.getUserId());
        postService.addPost(postDto, postImages);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //pobieranie zdjęcia z bazy
    @GetMapping(value = "/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable(value = "id") String imageId) {
        Image image = fileService.findImageById(imageId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"");
        return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
    }

}
