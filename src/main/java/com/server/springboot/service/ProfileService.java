package com.server.springboot.service;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.enumeration.FavouriteType;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import com.server.springboot.domain.enumeration.SchoolType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    UserProfileDto findProfileInformationByUserId(Long userId);

    UserActivityDto findUserActivityByUserId(Long userId);

    List<UserFavouriteDto> findFavouritesByUserId(Long userId);

    List<InterestDto> findInterestsByUserId(Long userId);

    List<ImageDto> findImagesByUserId(Long userId);

    List<InterestDto> findAllInterests();

    void editUserProfileInformation(UpdateUserProfileDto updateUserProfileDto);

    void addUserFavourite(RequestUserFavouriteDto requestUserFavouriteDto);

    void editUserFavouriteById(Long favouriteId, RequestUserFavouriteDto requestUserFavouriteDto);

    void deleteUserFavouriteById(Long favouriteId);

    List<FavouriteType> findAllFavourites();

    List<RelationshipStatus> findAllRelationshipStatus();

    void addUserInterestById(Long interestId);

    void deleteUserInterestById(Long interestId);

    ProfilePhotoDto findProfilePhotoByUserId(Long userId);

    @Transactional
    void updateUserProfilePhoto(MultipartFile photo, String caption);

    void deleteUserProfilePhoto();

    void editUserAddress(Long addressId, RequestAddressDto requestAddressDto);

    void addUserAddress(RequestAddressDto requestAddressDto);

    void createSchoolInformation(RequestSchoolDto requestSchoolDto);

    void editSchoolInformation(Long schoolId, RequestSchoolDto requestSchoolDto);

    void deleteSchoolInformation(Long schoolId);

    List<SchoolType> findAllSchools();

    void addUserWorkPlace(RequestWorkPlaceDto requestWorkPlaceDto);

    void editUserWorkPlace(Long workId, RequestWorkPlaceDto requestWorkPlaceDto);

    void deleteUserWorkPlace(Long workId);

}
