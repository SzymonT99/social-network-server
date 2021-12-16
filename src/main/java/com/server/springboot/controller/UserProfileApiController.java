package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.SharedEventDto;
import com.server.springboot.domain.dto.response.UserProfileDto;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserProfileApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);

    @ApiOperation(value = "Get user profile information by id")
    @GetMapping(value = "/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable(value = "userId") Long userId) {
        return new ResponseEntity<>(new UserProfileDto(), HttpStatus.OK);
    }

    @ApiOperation(value = "Update user basic profile information")
    @PutMapping(value = "/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateUserProfileDto updateUserProfileDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add user profile photo")
    @PostMapping(value = "/profile/photo", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addProfilePhoto(@RequestParam(value = "photo") MultipartFile photo) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update user address")
    @PutMapping(value = "/profile/address")
    public ResponseEntity<?> updateAddress(@Valid @RequestBody UpdateAddressDto updateAddressDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user school")
    @PostMapping(value = "/profile/schools")
    public ResponseEntity<?> addSchoolInfo(@Valid @RequestBody RequestSchoolDto requestSchoolDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user school by id")
    @PutMapping(value = "/profile/schools/{schoolId}")
    public ResponseEntity<?> updateSchoolInfo(@PathVariable(value = "schoolId") Long schoolId,
                                              @Valid @RequestBody RequestSchoolDto requestSchoolDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user school by id")
    @DeleteMapping(value = "/profile/schools/{schoolId}")
    public ResponseEntity<?> deleteSchoolInfo(@PathVariable(value = "schoolId") Long schoolId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user favourite")
    @PostMapping(value = "/profile/favourite")
    public ResponseEntity<?> addFavouriteInfo(@Valid @RequestBody RequestUserFavouriteDto requestUserFavouriteDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user favourite by id")
    @PutMapping(value = "/profile/favourite/{favouriteId}")
    public ResponseEntity<?> updateFavouriteInfo(@PathVariable(value = "favouriteId") Long favouriteId,
                                              @Valid @RequestBody RequestUserFavouriteDto requestUserFavouriteDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user favourite by id")
    @DeleteMapping(value = "/profile/favourite/{favouriteId}")
    public ResponseEntity<?> deleteFavouriteInfo(@PathVariable(value = "favouriteId") Long favouriteId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user work place")
    @PostMapping(value = "/profile/work")
    public ResponseEntity<?> addWorkPlaceInfo(@Valid @RequestBody RequestWorkPlaceDto requestWorkPlaceDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user work place by id")
    @PutMapping(value = "/profile/work/{workId}")
    public ResponseEntity<?> updateWorkPlaceInfo(@PathVariable(value = "workId") Long workId,
                                                 @Valid @RequestBody RequestWorkPlaceDto requestWorkPlaceDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user work place by id")
    @DeleteMapping(value = "/profile/work/{workId}")
    public ResponseEntity<?> deleteWorkPlaceInfo(@PathVariable(value = "workId") Long workId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
