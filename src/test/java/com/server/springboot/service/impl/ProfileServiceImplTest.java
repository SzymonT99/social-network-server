package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestAddressDto;
import com.server.springboot.domain.dto.request.RequestSchoolDto;
import com.server.springboot.domain.dto.request.RequestUserFavouriteDto;
import com.server.springboot.domain.dto.request.UpdateUserProfileDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFavouriteRepository userFavouriteRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private WorkPlaceRepository workPlaceRepository;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private FileService fileService;
    @Mock
    private RoleRepository roleRepository;
    @Spy
    private UserProfileDtoMapper userProfileDtoMapper;
    @Spy
    private UserActivityDtoMapper userActivityDtoMapper;
    @Spy
    private UserFavouriteDtoListMapper userFavouriteDtoListMapper;
    @Spy
    private InterestDtoListMapper interestDtoListMapper;
    @Spy
    private ImageDtoListMapper imageDtoListMapper;
    @Spy
    private UserFavouriteMapper userFavouriteMapper;
    @Spy
    private SchoolMapper schoolMapper;
    @Spy
    private WorkPlaceMapper workPlaceMapper;
    @Spy
    private AddressMapper addressMapper;
    @Spy
    private FriendDtoListMapper friendDtoListMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UserProfile userProfile;
    private User user;

    @BeforeEach
    void setUp() {

        userProfileDtoMapper = new UserProfileDtoMapper();
        userActivityDtoMapper = new UserActivityDtoMapper();
        userFavouriteDtoListMapper = new UserFavouriteDtoListMapper();
        interestDtoListMapper = new InterestDtoListMapper();
        imageDtoListMapper = new ImageDtoListMapper();
        userFavouriteMapper = new UserFavouriteMapper();
        schoolMapper = new SchoolMapper();
        workPlaceMapper = new WorkPlaceMapper();
        addressMapper = new AddressMapper();
        friendDtoListMapper = new FriendDtoListMapper();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        userProfile = UserProfile.builder()
                .userProfileId(1L)
                .user(User.builder()
                        .userId(1L)
                        .username("Jan123")
                        .password("Qwertyuiop")
                        .email("janNowak@gmail.com")
                        .phoneNumber("123456789")
                        .incorrectLoginCounter(0)
                        .createdAt(LocalDateTime.now())
                        .verifiedAccount(true)
                        .activityStatus(ActivityStatus.OFFLINE)
                        .userProfile(userProfile)
                        .roles(new HashSet<Role>() {{
                            add(new Role(1, AppRole.ROLE_USER));
                        }})
                        .build())
                .firstName("Jan")
                .lastName("Nowak")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.parse("1989-01-05", formatter))
                .age(LocalDate.now().getYear() - LocalDate.parse("1989-01-05", formatter).getYear())
                .isPublic(true)
                .aboutUser("opis")
                .favourites(new HashSet<>())
                .job("Tester")
                .relationshipStatus(RelationshipStatus.SINGLE)
                .skills("Testowanie")
                .workPlaces(new HashSet<>())
                .schools(new HashSet<>())
                .build();

        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(true)
                .activityStatus(ActivityStatus.OFFLINE)
                .userProfile(userProfile)
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .likedComments(new HashSet<>())
                .userInterests(new HashSet<>())
                .comments(new HashSet<>())
                .favouritePosts(new HashSet<>())
                .friends(new HashSet<>())
                .likedPosts(new HashSet<>())
                .sharedEvents(new HashSet<>())
                .sharedPosts(new HashSet<>())
                .posts(new HashSet<>())
                .memberOfGroups(new HashSet<>())
                .build();
    }

    @Test
    public void shouldFindProfileInformationByUserId() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileDto resultUserProfile = profileService.findProfileInformationByUserId(userId);

        assertNotNull(resultUserProfile);
        assertEquals(userProfileDtoMapper.convert(userProfile), resultUserProfile);
    }

    @Test
    public void shouldFindUserActivityByUserId() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserActivityDto resultUserActivity = profileService.findUserActivityByUserId(userId);

        assertNotNull(resultUserActivity);
        assertEquals(userProfile.getUserProfileId(), resultUserActivity.getUserProfileId());
    }

    @Test
    public void shouldFindFavouritesByUserId() {
        Long userId = 1L;
        List<UserFavourite> userFavourites = new ArrayList<>();
        UserFavourite userFavourite1 = UserFavourite.builder()
                .userFavouriteId(1L)
                .userProfile(userProfile)
                .favouriteType(FavouriteType.ACTOR)
                .name("Johnny Deep")
                .build();
        UserFavourite userFavourite2 = UserFavourite.builder()
                .userFavouriteId(2L)
                .userProfile(userProfile)
                .favouriteType(FavouriteType.FILM)
                .name("Star Wars")
                .build();
        userFavourites.add(userFavourite1);
        userFavourites.add(userFavourite2);

        userProfile.setFavourites(new HashSet<>(userFavourites));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userFavouriteRepository.findByUserProfile(userProfile)).thenReturn(userFavourites);

        List<UserFavouriteDto> resultUserFavourites = profileService.findFavouritesByUserId(userId);

        assertEquals(2, resultUserFavourites.size());
        assertEquals(userFavouriteDtoListMapper.convert(userFavourites), resultUserFavourites);
    }

    @Test
    public void shouldFindInterestsByUserId() {
        Long userId = 1L;
        List<Interest> userInterests = new ArrayList<>();
        Interest interest1 = Interest.builder()
                .interestId(1L)
                .name("Sport")
                .build();
        Interest interest2 = Interest.builder()
                .interestId(2L)
                .name("Programming")
                .build();
        userInterests.add(interest1);
        userInterests.add(interest2);

        user.setUserInterests(new HashSet<>(userInterests));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<InterestDto> resultUserInterests = profileService.findInterestsByUserId(userId);

        assertEquals(2, resultUserInterests.size());
        assertEquals(interestDtoListMapper.convert(userInterests), resultUserInterests);
    }

    @Test
    public void shouldFindImagesByUserId() {
        Long userId = 1L;
        List<Image> userImages = new ArrayList<>();
        Image image1 = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(userProfile)
                .addedIn(LocalDateTime.now())
                .filePath("/uploads/image1")
                .filename("image1.png")
                .build();
        Image image2 = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(userProfile)
                .addedIn(LocalDateTime.now().minusHours(1L))
                .filePath("/uploads/image2")
                .filename("image2.png")
                .build();
        userImages.add(image1);
        userImages.add(image2);

        userProfile.setUserImages(new HashSet<>(userImages));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<ImageDto> resultUserImages = profileService.findImagesByUserId(userId);

        assertEquals(2, resultUserImages.size());
        assertEquals(imageDtoListMapper.convert(userImages), resultUserImages);
    }

    @Test
    public void shouldEditUserProfileInformation() {
        Long userId = 1L;

        UpdateUserProfileDto updateUserProfileDto = UpdateUserProfileDto.builder()
                .firstName("Piotr")
                .lastName("Kowalski")
                .isPublic(false)
                .gender(Gender.MALE.toString())
                .relationshipStatus(RelationshipStatus.SINGLE.toString())
                .dateOfBirth("1989-01-01")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser(any(User.class))).thenReturn(Optional.of(userProfile));

        profileService.editUserProfileInformation(userId, updateUserProfileDto);

        verify(userProfileRepository, times(1)).save(userProfile);
    }

    @Test
    public void shouldAddUserFavourite() {
        RequestUserFavouriteDto requestUserFavouriteDto = RequestUserFavouriteDto.builder()
                .favouriteType(FavouriteType.FILM.toString())
                .name("Harry Pother")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        profileService.addUserFavourite(requestUserFavouriteDto);

        verify(userFavouriteRepository, times(1)).save(any(UserFavourite.class));
    }

    @Test
    public void shouldEditUserFavouriteById() {
        Long favouriteId = 1L;
        RequestUserFavouriteDto requestUserFavouriteDto = RequestUserFavouriteDto.builder()
                .favouriteType(FavouriteType.FILM.toString())
                .name("Harry Pother")
                .build();

        UserFavourite savedFavourite = UserFavourite.builder()
                .userFavouriteId(1L)
                .userProfile(userProfile)
                .favouriteType(FavouriteType.FILM)
                .name("Star Wars")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userFavouriteRepository.findById(favouriteId)).thenReturn(Optional.of(savedFavourite));

        profileService.editUserFavouriteById(favouriteId, requestUserFavouriteDto);

        verify(userFavouriteRepository, times(1)).save(savedFavourite);
    }

    @Test
    public void shouldDeleteUserFavouriteById() {
        Long favouriteId = 1L;

        UserFavourite savedFavourite = UserFavourite.builder()
                .userFavouriteId(1L)
                .userProfile(userProfile)
                .favouriteType(FavouriteType.FILM)
                .name("Star Wars")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userFavouriteRepository.findById(favouriteId)).thenReturn(Optional.of(savedFavourite));
        doNothing().when(userFavouriteRepository).deleteById(favouriteId);

        profileService.deleteUserFavouriteById(favouriteId);

        verify(userFavouriteRepository, times(1)).deleteById(favouriteId);
    }

    @Test
    public void shouldFindProfilePhotoByUserId() {
        Long userId = 1L;

        Image userPhoto = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(userProfile)
                .addedIn(LocalDateTime.now())
                .filePath("/uploads/profilePhoto")
                .filename("photo.png")
                .build();
        userProfile.setProfilePhoto(userPhoto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ProfilePhotoDto resultProfilePhoto = profileService.findProfilePhotoByUserId(userId);

        assertNotNull(resultProfilePhoto);
        assertEquals("localhost:8080/api/images/" + userPhoto.getImageId(), resultProfilePhoto.getUrl());
    }

    @Test
    public void shouldUpdateUserProfilePhoto() {
        Long userId = 1L;
        MockMultipartFile newImage = new MockMultipartFile("photoChanged", new byte[1]);

        Image lastPhoto = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(userProfile)
                .addedIn(LocalDateTime.now())
                .filePath("/uploads/profilePhoto")
                .filename("photo.png")
                .build();
        userProfile.setProfilePhoto(lastPhoto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(fileService.storageOneImage(newImage, user, true)).thenReturn(new Image());

        profileService.updateUserProfilePhoto(userId, newImage);

        verify(imageRepository, times(1)).delete(lastPhoto);
        verify(userProfileRepository, times(1)).save(userProfile);
    }

    @Test
    public void shouldDeleteUserProfilePhoto() throws IOException {
        Long userId = 1L;

        Image savedPhoto = Image.builder()
                .imageId(UUID.randomUUID().toString())
                .userProfile(userProfile)
                .addedIn(LocalDateTime.now())
                .filePath("/uploads/profilePhoto")
                .filename("photo.png")
                .build();
        userProfile.setProfilePhoto(savedPhoto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser(user)).thenReturn(Optional.of(userProfile));
        doNothing().when(imageRepository).delete(savedPhoto);
        doNothing().when(fileService).deleteImage(savedPhoto.getImageId());

        profileService.deleteUserProfilePhoto(userId);

        verify(imageRepository, times(1)).delete(savedPhoto);
        verify(fileService, times(1)).deleteImage(savedPhoto.getImageId());
        verify(userProfileRepository, times(1)).save(userProfile);
    }

    @Test
    public void shouldAddUserAddress() {
        RequestAddressDto requestAddressDto = RequestAddressDto.builder()
                .country("Polska")
                .city("Tarnów")
                .street("ul. A. Mickiewicza")
                .zipCode("33-100")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            Address address = (Address) invocation.getArgument(0);
            assertEquals("Polska", address.getCountry());
            assertEquals("Tarnów", address.getCity());
            assertEquals("ul. A. Mickiewicza", address.getStreet());
            return null;
        }).when(addressRepository).save(any(Address.class));

        profileService.addUserAddress(requestAddressDto);

        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    public void shouldEditUserAddress() {
        Long addressId = 1L;
        RequestAddressDto requestAddressDto = RequestAddressDto.builder()
                .country("Polska")
                .city("Tarnów")
                .street("ul. Narutowicza")  // zmiana
                .zipCode("33-100")
                .build();
        Address savedAddress = Address.builder()
                .addressId(1L)
                .country("Polska")
                .city("Tarnów")
                .street("ul. A. Mickiewicza")
                .zipCode("33-100")
                .userProfile(userProfile)
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(savedAddress));

        doAnswer(invocation -> {
            Address updatedAddress = (Address) invocation.getArgument(0);
            assertEquals(requestAddressDto.getStreet(), updatedAddress.getStreet());
            return null;
        }).when(addressRepository).save(any(Address.class));

        profileService.editUserAddress(addressId, requestAddressDto);

        verify(addressRepository, times(1)).save(savedAddress);
    }

    @Test
    public void shouldCreateSchoolInformation() {
        RequestSchoolDto requestSchoolDto = RequestSchoolDto.builder()
                .name("PWSZ")
                .schoolType(SchoolType.UNIVERSITY.toString())
                .startDate(LocalDate.now().minusYears(3L).toString())
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            School addedSchool = (School) invocation.getArgument(0);
            assertEquals(requestSchoolDto.getName(), addedSchool.getName());
            assertEquals(requestSchoolDto.getSchoolType(), addedSchool.getSchoolType().toString());
            return null;
        }).when(schoolRepository).save(any(School.class));

        profileService.createSchoolInformation(requestSchoolDto);

        verify(schoolRepository, times(1)).save(any(School.class));
    }

    @Test
    public void shouldEditSchoolInformation() {
        Long schoolId = 1L;
        RequestSchoolDto requestSchoolDto = RequestSchoolDto.builder()
                .name("ANS")    // zmiana
                .schoolType(SchoolType.UNIVERSITY.toString())
                .startDate(LocalDate.now().minusYears(3L).toString())
                .build();
        School savedSchool = School.builder()
                .schoolId(1L)
                .name("PWSZ")
                .schoolType(SchoolType.UNIVERSITY)
                .startDate(LocalDate.now().minusYears(3L))
                .userProfile(userProfile)
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(savedSchool));

        doAnswer(invocation -> {
            School updatedSchool = (School) invocation.getArgument(0);
            assertEquals(requestSchoolDto.getName(), updatedSchool.getName());
            return null;
        }).when(schoolRepository).save(any(School.class));

        profileService.editSchoolInformation(schoolId, requestSchoolDto);

        verify(schoolRepository, times(1)).save(savedSchool);
    }

    @Test
    public void shouldDeleteSchoolInformation() {
        Long schoolId = 1L;

        School savedSchool = School.builder()
                .schoolId(1L)
                .name("PWSZ")
                .schoolType(SchoolType.UNIVERSITY)
                .startDate(LocalDate.now().minusYears(3L))
                .userProfile(userProfile)
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(savedSchool));

        profileService.deleteSchoolInformation(schoolId);

        verify(schoolRepository, times(1)).deleteById(schoolId);
    }
}

