package com.server.springboot.service.impl;

import com.google.common.collect.Lists;
import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.BadRequestException;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserFavouriteRepository userFavouriteRepository;
    private final InterestRepository interestRepository;
    private final UserProfileRepository userProfileRepository;
    private final ImageRepository imageRepository;
    private final AddressRepository addressRepository;
    private final SchoolRepository schoolRepository;
    private final WorkPlaceRepository workPlaceRepository;
    private final FriendRepository friendRepository;
    private final JwtUtils jwtUtils;
    private final FileService fileService;
    private final UserProfileDtoMapper userProfileDtoMapper;
    private final UserActivityDtoMapper userActivityDtoMapper;
    private final UserFavouriteDtoListMapper userFavouriteDtoListMapper;
    private final InterestDtoListMapper interestDtoListMapper;
    private final ImageDtoListMapper imageDtoListMapper;
    private final UserFavouriteMapper userFavouriteMapper;
    private final SchoolMapper schoolMapper;
    private final WorkPlaceMapper workPlaceMapper;
    private final AddressMapper addressMapper;
    private final FriendDtoListMapper friendDtoListMapper;
    private final RoleRepository roleRepository;

    @Autowired
    public ProfileServiceImpl(UserRepository userRepository, UserFavouriteRepository userFavouriteRepository,
                              InterestRepository interestRepository, UserProfileRepository userProfileRepository,
                              ImageRepository imageRepository, AddressRepository addressRepository,
                              SchoolRepository schoolRepository, WorkPlaceRepository workPlaceRepository,
                              FriendRepository friendRepository, JwtUtils jwtUtils,
                              FileService fileService, UserProfileDtoMapper userProfileDtoMapper,
                              UserActivityDtoMapper userActivityDtoMapper, UserFavouriteDtoListMapper userFavouriteDtoListMapper,
                              InterestDtoListMapper interestDtoListMapper, ImageDtoListMapper imageDtoListMapper,
                              UserFavouriteMapper userFavouriteMapper, SchoolMapper schoolMapper,
                              WorkPlaceMapper workPlaceMapper, AddressMapper addressMapper,
                              FriendDtoListMapper friendDtoListMapper, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userFavouriteRepository = userFavouriteRepository;
        this.interestRepository = interestRepository;
        this.userProfileRepository = userProfileRepository;
        this.imageRepository = imageRepository;
        this.addressRepository = addressRepository;
        this.schoolRepository = schoolRepository;
        this.workPlaceRepository = workPlaceRepository;
        this.friendRepository = friendRepository;
        this.jwtUtils = jwtUtils;
        this.fileService = fileService;
        this.userProfileDtoMapper = userProfileDtoMapper;
        this.userActivityDtoMapper = userActivityDtoMapper;
        this.userFavouriteDtoListMapper = userFavouriteDtoListMapper;
        this.interestDtoListMapper = interestDtoListMapper;
        this.imageDtoListMapper = imageDtoListMapper;
        this.userFavouriteMapper = userFavouriteMapper;
        this.schoolMapper = schoolMapper;
        this.workPlaceMapper = workPlaceMapper;
        this.addressMapper = addressMapper;
        this.friendDtoListMapper = friendDtoListMapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserProfileDto findProfileInformationByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserProfile userProfile = user.getUserProfile();
        return userProfileDtoMapper.convert(userProfile);
    }

    @Override
    public UserActivityDto findUserActivityByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        List<Friend> friends = friendRepository.findByUserAndIsInvitationAccepted(user, true);
        UserActivityDto userActivityDto = userActivityDtoMapper.convert(user);
        userActivityDto.setFriends(friendDtoListMapper.convert(friends));
        return userActivityDto;
    }

    @Override
    public List<UserFavouriteDto> findFavouritesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<UserFavourite> userFavourites = userFavouriteRepository.findByUserProfile(user.getUserProfile());
        return userFavouriteDtoListMapper.convert(userFavourites);
    }

    @Override
    public List<InterestDto> findInterestsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<Interest> interests = Lists.newArrayList(user.getUserInterests());
        return interestDtoListMapper.convert(interests);
    }

    @Override
    public List<ImageDto> findImagesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        List<Image> addedImages = Lists.newArrayList(user.getUserProfile().getUserImages());
        return imageDtoListMapper.convert(addedImages);
    }

    @Override
    public List<InterestDto> findAllInterests() {
        return interestDtoListMapper.convert(interestRepository.findAll());
    }

    @Override
    public void editUserProfileInformation(Long userId, UpdateUserProfileDto updateUserProfileDto) {
        Long loggedUserId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(loggedUserId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + loggedUserId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        if (!loggedUserId.equals(userId) && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new NotFoundException("No access to edit user profile");
        }

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Not found user profile with user id: " + userId));
        userProfile.setPublic(updateUserProfileDto.isPublic());
        userProfile.setFirstName(updateUserProfileDto.getFirstName());
        userProfile.setLastName(updateUserProfileDto.getLastName());
        userProfile.setAboutUser(updateUserProfileDto.getAboutUser());
        userProfile.setGender(Gender.valueOf(updateUserProfileDto.getGender()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        userProfile.setDateOfBirth(LocalDate.parse(updateUserProfileDto.getDateOfBirth(), formatter));

        userProfile.setJob(updateUserProfileDto.getJob());
        userProfile.setRelationshipStatus(RelationshipStatus.valueOf(updateUserProfileDto.getRelationshipStatus()));
        userProfile.setSkills(updateUserProfileDto.getSkills());

        userProfileRepository.save(userProfile);
    }

    @Override
    public void addUserFavourite(RequestUserFavouriteDto requestUserFavouriteDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserFavourite userFavourite = userFavouriteMapper.convert(requestUserFavouriteDto);
        userFavourite.setUserProfile(user.getUserProfile());
        userFavouriteRepository.save(userFavourite);
    }

    @Override
    public void editUserFavouriteById(Long favouriteId, RequestUserFavouriteDto requestUserFavouriteDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserFavourite userFavourite = userFavouriteRepository.findById(favouriteId)
                .orElseThrow(() -> new NotFoundException("Not found user favourite with id: " + favouriteId));

        if (!userFavourite.getUserProfile().getUser().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("User favourite editing access forbidden");
        }
        userFavourite.setName(requestUserFavouriteDto.getName());
        userFavourite.setFavouriteType(FavouriteType.valueOf(requestUserFavouriteDto.getFavouriteType()));
        userFavouriteRepository.save(userFavourite);
    }

    @Override
    public void deleteUserFavouriteById(Long favouriteId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserFavourite userFavourite = userFavouriteRepository.findById(favouriteId)
                .orElseThrow(() -> new NotFoundException("Not found user favourite with id: " + favouriteId));
        if (!userFavourite.getUserProfile().getUser().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("User favourite deleting access forbidden");
        }
        userFavouriteRepository.deleteById(favouriteId);
    }

    @Override
    public void addUserInterestById(Long userId, Long interestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Interest addedInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new NotFoundException("Not found interest with id: " + interestId));
        Set<Interest> userInterests = user.getUserInterests();
        if (userInterests.contains(addedInterest)) {
            throw new ConflictRequestException("The given interest has already been added to the user's interests");
        }
        userInterests.add(addedInterest);
        user.setUserInterests(userInterests);
        userRepository.save(user);
    }

    @Override
    public void deleteUserInterestById(Long userId, Long interestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Interest deletedInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new NotFoundException("Not found interest with id: " + interestId));
        Set<Interest> userInterests = user.getUserInterests();
        if (!userInterests.contains(deletedInterest) &&
                !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new BadRequestException("The interest given does not belong to the interests of the user");
        }
        userInterests.remove(deletedInterest);
        user.setUserInterests(userInterests);
        userRepository.save(user);
    }

    @Override
    public ProfilePhotoDto findProfilePhotoByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Image profilePhoto = user.getUserProfile().getProfilePhoto();
        if (profilePhoto == null) {
            throw new NotFoundException("Not found any profile photo for user with id: " + userId);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return ProfilePhotoDto.builder()
                .filename(profilePhoto.getFilename())
                .url("localhost:8080/api/images/" + profilePhoto.getImageId())
                .type(profilePhoto.getType())
                .addedIn(profilePhoto.getAddedIn().format(formatter))
                .build();
    }

    @Override
    public void updateUserProfilePhoto(Long userId, MultipartFile photo) {
        if (photo.isEmpty()) {
            throw new BadRequestException("Profile photo not sent");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserProfile userProfile = user.getUserProfile();
        if (userProfile.getProfilePhoto() != null) {
            try {
                fileService.deleteImage(userProfile.getProfilePhoto().getImageId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageRepository.delete(userProfile.getProfilePhoto());
        }
        Image image = fileService.storageOneImage(photo, user, true);

        Post changePhotoPost = Post.builder()
                .isCommentingBlocked(false)
                .isPublic(false)
                .createdAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(false)
                .postAuthor(user)
                .images(Collections.singleton(image))
                .build();

        image.setChangePhotoPost(changePhotoPost);
        userProfile.setProfilePhoto(image);
        userProfileRepository.save(userProfile);
    }

    @Override
    public void deleteUserProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Not found user profile with user id: " + userId));

        Image userProfilePhoto = userProfile.getProfilePhoto();
        userProfile.setProfilePhoto(null);
        userProfileRepository.save(userProfile);
        if (!userProfilePhoto.getUserProfile().getUser().getUserId().equals(userId)
                && !user.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - profile photo deleting access forbidden");
        }
        try {
            fileService.deleteImage(userProfilePhoto.getImageId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageRepository.delete(userProfilePhoto);
    }

    @Override
    public void addUserAddress(RequestAddressDto requestAddressDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        Address address = addressMapper.convert(requestAddressDto);
        UserProfile userProfile = user.getUserProfile();
        userProfile.setAddress(address);
        addressRepository.save(address);
    }

    @Override
    public void editUserAddress(Long addressId, RequestAddressDto requestAddressDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Not found address with id: " + addressId));
        if (!address.getUserProfile().getUser().getUserId().equals(userId)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - address editing access forbidden");
        }
        address.setCountry(requestAddressDto.getCountry());
        address.setCity(requestAddressDto.getCity());
        address.setStreet(requestAddressDto.getStreet());
        address.setZipCode(requestAddressDto.getZipCode());
        addressRepository.save(address);
    }

    @Override
    public void createSchoolInformation(RequestSchoolDto requestSchoolDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        School addedSchool = schoolMapper.convert(requestSchoolDto);
        addedSchool.setUserProfile(user.getUserProfile());
        schoolRepository.save(addedSchool);
    }

    @Override
    public void editSchoolInformation(Long schoolId, RequestSchoolDto requestSchoolDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("Not found school with id: " + schoolId));
        if (!school.getUserProfile().getUser().getUserId().equals(userId)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - school editing access forbidden");
        }
        school.setSchoolType(SchoolType.valueOf(requestSchoolDto.getSchoolType()));
        school.setName(requestSchoolDto.getName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        school.setStartDate(LocalDate.parse(requestSchoolDto.getStartDate(), formatter));
        school.setGraduationDate(requestSchoolDto.getGraduationDate() != null
                ? LocalDate.parse(requestSchoolDto.getGraduationDate(), formatter) : null);
        schoolRepository.save(school);
    }

    @Override
    public void deleteSchoolInformation(Long schoolId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("Not found school with id: " + schoolId));
        if (!school.getUserProfile().getUser().getUserId().equals(userId)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - school deleting access forbidden");
        }
        schoolRepository.deleteById(schoolId);
    }

    @Override
    public void addUserWorkPlace(RequestWorkPlaceDto requestWorkPlaceDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        WorkPlace workPlace = workPlaceMapper.convert(requestWorkPlaceDto);
        workPlace.setUserProfile(user.getUserProfile());
        workPlaceRepository.save(workPlace);
    }

    @Override
    public void editUserWorkPlace(Long workId, RequestWorkPlaceDto requestWorkPlaceDto) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        WorkPlace workPlace = workPlaceRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Not found work place with id: " + workId));
        if (!workPlace.getUserProfile().getUser().getUserId().equals(userId)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - work place editing access forbidden");
        }
        workPlace.setCompany(requestWorkPlaceDto.getCompany());
        workPlace.setPosition(requestWorkPlaceDto.getPosition());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        workPlace.setStartDate(LocalDate.parse(requestWorkPlaceDto.getStartDate(), formatter));
        workPlace.setEndDate(requestWorkPlaceDto.getStartDate() != null
                ? LocalDate.parse(requestWorkPlaceDto.getStartDate(), formatter) : null);
        workPlaceRepository.save(workPlace);
    }

    @Override
    public void deleteUserWorkPlace(Long workId) {
        Long userId = jwtUtils.getLoggedInUserId();
        User loggedUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
        WorkPlace workPlace = workPlaceRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Not found work place with id: " + workId));
        if (!workPlace.getUserProfile().getUser().getUserId().equals(userId)
                && !loggedUser.getRoles().contains(roleRepository.findByName(AppRole.ROLE_ADMIN).get())) {
            throw new ForbiddenException("Invalid logged user id - work place editing access forbidden");
        }
        workPlaceRepository.delete(workPlace);
    }
}
