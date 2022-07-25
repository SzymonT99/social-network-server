package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.service.ProfileService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserProfileApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final ProfileService profileService;

    @Autowired
    public UserProfileApiController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @ApiOperation(value = "Get user profile information by id")
    @GetMapping(value = "/profile/{userId}/information")
    public ResponseEntity<UserProfileDto> getUserProfileInformation(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- User get profile information");
        return new ResponseEntity<>(profileService.findProfileInformationByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Update user basic profile information")
    @PutMapping(value = "/profile/{userId}/information")
    public ResponseEntity<?> updateProfileInformation(@PathVariable(value = "userId") Long userId,
                                                      @Valid @RequestBody UpdateUserProfileDto updateUserProfileDto) {
        LOGGER.info("---- Update profile information for user with id: {}", userId);
        profileService.editUserProfileInformation(userId, updateUserProfileDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get user activity")
    @GetMapping(value = "/profile/{userId}/activity")
    public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- User get information about activity");
        return new ResponseEntity<>(profileService.findUserActivityByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get user favourites")
    @GetMapping(value = "/profile/{userId}/favourites")
    public ResponseEntity<List<UserFavouriteDto>> getUserFavourites(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- User get information about own favourites");
        return new ResponseEntity<>(profileService.findFavouritesByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user favourite")
    @PostMapping(value = "/profile/favourites")
    public ResponseEntity<?> addFavouriteInfo(@Valid @RequestBody RequestUserFavouriteDto requestUserFavouriteDto) {
        LOGGER.info("---- User add favourite item");
        profileService.addUserFavourite(requestUserFavouriteDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user favourite by id")
    @PutMapping(value = "/profile/favourites/{favouriteId}")
    public ResponseEntity<?> updateFavouriteInfo(@PathVariable(value = "favouriteId") Long favouriteId,
                                                 @Valid @RequestBody RequestUserFavouriteDto requestUserFavouriteDto) {
        LOGGER.info("---- User update one of own favourite item");
        profileService.editUserFavouriteById(favouriteId, requestUserFavouriteDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user favourite by id")
    @DeleteMapping(value = "/profile/favourites/{favouriteId}")
    public ResponseEntity<?> deleteFavouriteInfo(@PathVariable(value = "favouriteId") Long favouriteId) {
        LOGGER.info("---- User deletes one of own favourite item");
        profileService.deleteUserFavouriteById(favouriteId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get user interests")
    @GetMapping(value = "/profile/{userId}/interests")
    public ResponseEntity<List<InterestDto>> getUserInterests(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- User get information about own interests");
        return new ResponseEntity<>(profileService.findInterestsByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get possible user interests")
    @GetMapping(value = "/interests")
    public ResponseEntity<List<InterestDto>> getInterests() {
        LOGGER.info("---- Get all interests");
        return new ResponseEntity<>(profileService.findAllInterests(), HttpStatus.OK);
    }

    @ApiOperation(value = "Add user interests")
    @PostMapping(value = "/profile/{userId}/interests/{interestId}")
    public ResponseEntity<?> addUserInterest(@PathVariable(value = "userId") Long userId,
                                             @PathVariable(value = "interestId") Long interestId) {
        LOGGER.info("---- Add user interest");
        profileService.addUserInterestById(userId, interestId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete user interests")
    @DeleteMapping(value = "/profile/{userId}/interests/{interestId}")
    public ResponseEntity<?> deleteUserInterest(@PathVariable(value = "userId") Long userId,
                                                @PathVariable(value = "interestId") Long interestId) {
        LOGGER.info("---- Delete user interest");
        profileService.deleteUserInterestById(userId, interestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get images added by user")
    @GetMapping(value = "/profile/{userId}/images")
    public ResponseEntity<List<ImageDto>> getUserImages(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- User get information about own favourites");
        return new ResponseEntity<>(profileService.findImagesByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get user profile photo")
    @GetMapping(value = "/profile/{userId}/photo")
    public ResponseEntity<ProfilePhotoDto> getUserProfilePhoto(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- Get user's profile photo");
        return new ResponseEntity<>(profileService.findProfilePhotoByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Add user profile photo")
    @PutMapping(value = "/profile/{userId}/photo")
    public ResponseEntity<?> addProfilePhoto(@PathVariable(value = "userId") Long userId,
                                             @RequestParam(value = "photo") MultipartFile photo) {
        LOGGER.info("---- Update user's profile photo");
        profileService.updateUserProfilePhoto(userId, photo);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user profile photo")
    @DeleteMapping(value = "/profile/{userId}/photo")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- Delete user's profile photo");
        profileService.deleteUserProfilePhoto(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add user address")
    @PostMapping(value = "/profile/address")
    public ResponseEntity<?> addAddress(@Valid @RequestBody RequestAddressDto requestAddressDto) {
        LOGGER.info("---- User add address");
        profileService.addUserAddress(requestAddressDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update user address")
    @PutMapping(value = "/profile/address/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable(value = "addressId") Long addressId,
                                           @Valid @RequestBody RequestAddressDto requestAddressDto) {
        LOGGER.info("---- User updates address");
        profileService.editUserAddress(addressId, requestAddressDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user school")
    @PostMapping(value = "/profile/schools")
    public ResponseEntity<?> addSchoolInfo(@Valid @RequestBody RequestSchoolDto requestSchoolDto) {
        LOGGER.info("---- User creates school information");
        profileService.createSchoolInformation(requestSchoolDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user school by id")
    @PutMapping(value = "/profile/schools/{schoolId}")
    public ResponseEntity<?> updateSchoolInfo(@PathVariable(value = "schoolId") Long schoolId,
                                              @Valid @RequestBody RequestSchoolDto requestSchoolDto) {
        LOGGER.info("---- User updates school information with id: {}", schoolId);
        profileService.editSchoolInformation(schoolId, requestSchoolDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user school by id")
    @DeleteMapping(value = "/profile/schools/{schoolId}")
    public ResponseEntity<?> deleteSchoolInfo(@PathVariable(value = "schoolId") Long schoolId) {
        LOGGER.info("---- User deletes school information with id: {}", schoolId);
        profileService.deleteSchoolInformation(schoolId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add info about user work place")
    @PostMapping(value = "/profile/work")
    public ResponseEntity<?> addWorkPlaceInfo(@Valid @RequestBody RequestWorkPlaceDto requestWorkPlaceDto) {
        LOGGER.info("---- User adds workplace");
        profileService.addUserWorkPlace(requestWorkPlaceDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update info about user work place by id")
    @PutMapping(value = "/profile/work/{workId}")
    public ResponseEntity<?> updateWorkPlaceInfo(@PathVariable(value = "workId") Long workId,
                                                 @Valid @RequestBody RequestWorkPlaceDto requestWorkPlaceDto) {
        LOGGER.info("---- User updates workplace");
        profileService.editUserWorkPlace(workId, requestWorkPlaceDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete info about user work place by id")
    @DeleteMapping(value = "/profile/work/{workId}")
    public ResponseEntity<?> deleteWorkPlaceInfo(@PathVariable(value = "workId") Long workId) {
        LOGGER.info("---- User deletes workplace");
        profileService.deleteUserWorkPlace(workId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
