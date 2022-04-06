package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.request.RequestGroupDto;
import com.server.springboot.domain.dto.request.RequestGroupRuleDto;
import com.server.springboot.domain.dto.request.RequestThreadDto;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.*;
import com.server.springboot.domain.enumeration.*;
import com.server.springboot.domain.mapper.*;
import com.server.springboot.domain.repository.*;
import com.server.springboot.exception.ConflictRequestException;
import com.server.springboot.exception.ForbiddenException;
import com.server.springboot.exception.NotFoundException;
import com.server.springboot.security.JwtUtils;
import com.server.springboot.service.FileService;
import com.server.springboot.service.NotificationService;
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupRuleRepository groupRuleRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileService fileService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private GroupThreadRepository groupThreadRepository;
    @Mock
    private ThreadAnswerRepository threadAnswerRepository;
    @Mock
    private ThreadAnswerReviewRepository threadAnswerReviewRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RoleRepository roleRepository;
    @Spy
    private GroupMapper groupMapper;
    @Spy
    private GroupDtoListMapper groupDtoListMapper;
    @Spy
    private GroupDetailsDtoMapper groupDetailsDtoMapper;
    @Spy
    private GroupRuleMapper groupRuleMapper;
    @Spy
    private GroupInvitationDtoListMapper groupInvitationDtoListMapper;
    @Spy
    private PostDtoListMapper postDtoListMapper;
    @Spy
    private ThreadMapper groupThreadMapper;
    @Spy
    private UserDtoMapper userDtoMapper;
    @Spy
    private UserDtoListMapper userDtoListMapper;
    @Spy
    private GroupMemberDtoListMapper groupMemberDtoListMapper;
    @Spy
    private GroupThreadDtoListMapper groupThreadDtoListMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private User user;
    private GroupMember groupMember;
    private Group group;

    @BeforeEach
    void setUp() {

        groupMapper = new GroupMapper();
        groupDtoListMapper = new GroupDtoListMapper();
        groupDetailsDtoMapper = new GroupDetailsDtoMapper();
        groupRuleMapper = new GroupRuleMapper();
        groupInvitationDtoListMapper = new GroupInvitationDtoListMapper();
        postDtoListMapper = new PostDtoListMapper();
        groupThreadMapper = new ThreadMapper();
        userDtoMapper = new UserDtoMapper();
        userDtoListMapper = new UserDtoListMapper();
        groupMemberDtoListMapper = new GroupMemberDtoListMapper();
        groupThreadDtoListMapper = new GroupThreadDtoListMapper();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        user = User.builder()
                .userId(1L)
                .username("Jan123")
                .password("Qwertyuiop")
                .email("janNowak@gmail.com")
                .phoneNumber("123456789")
                .incorrectLoginCounter(0)
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isBanned(false)
                .memberOfGroups(new HashSet<>())
                .userProfile(UserProfile.builder()
                        .firstName("Jan")
                        .lastName("Nowak")
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.parse("1989-01-05", formatter))
                        .age(LocalDate.now().getYear() - LocalDate.parse("1989-01-05", formatter).getYear())
                        .build()
                )
                .roles(new HashSet<Role>() {{
                    add(new Role(1, AppRole.ROLE_USER));
                }})
                .build();

        group = Group.builder()
                .groupId(1L)
                .name("Grupa")
                .description("opis")
                .groupRules(new HashSet<>())
                .groupInterests(new HashSet<>())
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .groupCreator(new User(user))
                .groupMembers(new HashSet<>())
                .groupThreads(new HashSet<>())
                .posts(new HashSet<>())
                .build();

        groupMember = GroupMember.builder()
                .groupMemberId(1L)
                .group(group)
                .member(user)
                .addedIn(LocalDateTime.now().minusDays(1L))
                .groupMemberStatus(GroupMemberStatus.JOINED)
                .groupPermissionType(GroupPermissionType.ADMINISTRATOR)
                .build();
        Set<GroupMember> groupMembers = new HashSet<>();
        groupMembers.add(groupMember);
        group.setGroupMembers(groupMembers);
    }

    @Test
    public void shouldCreateGroup() {
        RequestGroupDto requestGroupDto = RequestGroupDto.builder()
                .name("Grupa")
                .description("opis")
                .isPublic("true")
                .interests(new ArrayList<>())
                .build();
        MockMultipartFile groupImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileService.storageOneImage(groupImage, user, false)).thenReturn(new Image());

        GroupDetailsDto createdGroup = groupService.createGroup(requestGroupDto, groupImage);
        assertNotNull(createdGroup);
        assertTrue(createdGroup.isPublic());
        assertNotNull(createdGroup.getImage());

        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    public void shouldEditGroupWhenUserIsGroupAdministrator() throws IOException {
        Long groupId = 1L;
        RequestGroupDto requestGroupDto = RequestGroupDto.builder()
                .name("Grupa edytowana")    //zmiana
                .description("opis")
                .isPublic("true")
                .interests(new ArrayList<>())
                .build();
        MockMultipartFile groupImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        doAnswer(invocation -> {
            Group updatedGroup = (Group) invocation.getArgument(0);
            assertEquals(requestGroupDto.getName(), updatedGroup.getName());
            return null;
        }).when(groupRepository).save(any(Group.class));

        groupService.editGroup(groupId, requestGroupDto, groupImage);

        verify(groupRepository, times(1)).save(group);
    }

    @Test
    public void shouldThrowErrorWhenUserEditGroupAndIsNotGroupMember() {
        Long groupId = 1L;
        RequestGroupDto requestGroupDto = RequestGroupDto.builder()
                .name("Grupa edytowana")    //zmiana
                .description("opis")
                .isPublic("true")
                .interests(new ArrayList<>())
                .build();
        MockMultipartFile groupImage = new MockMultipartFile("image", new byte[1]);
        group.setGroupMembers(new HashSet<>());

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> {
                    groupService.editGroup(groupId, requestGroupDto, groupImage);
                }).withMessage("Logged in user is not a member of the group with id: " + groupId);

        verify(groupRepository, never()).save(group);
    }

    @Test
    public void shouldThrowErrorWhenUserEditGroupAndHasNotPermission() {
        Long groupId = 1L;
        RequestGroupDto requestGroupDto = RequestGroupDto.builder()
                .name("Grupa edytowana")    //zmiana
                .description("opis")
                .isPublic("true")
                .interests(new ArrayList<>())
                .build();
        MockMultipartFile groupImage = new MockMultipartFile("image", new byte[1]);
        groupMember.setGroupPermissionType(GroupPermissionType.MODERATOR);  // Moderator nie może edytować informacji

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        assertThatExceptionOfType(ForbiddenException.class)
                .isThrownBy(() -> {
                    groupService.editGroup(groupId, requestGroupDto, groupImage);
                }).withMessage("No access to edit group information");

        verify(groupRepository, never()).save(group);
    }

    @Test
    public void shouldEditGroupWhenUserIsNotMemberButIsAdmin() throws IOException {
        Long groupId = 1L;
        RequestGroupDto requestGroupDto = RequestGroupDto.builder()
                .name("Grupa edytowana")    //zmiana
                .description("opis")
                .isPublic("true")
                .interests(new ArrayList<>())
                .build();
        MockMultipartFile groupImage = new MockMultipartFile("image", new byte[1]);
        group.setGroupMembers(new HashSet<>()); // user nie jest członkiem grupy
        Set<Role> roles = new HashSet<>();
        Role roleUser = new Role(1, AppRole.ROLE_USER);
        Role roleAdmin = new Role(2, AppRole.ROLE_ADMIN);
        roles.add(roleUser);
        roles.add(roleAdmin);
        user.setRoles(roles);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(roleAdmin));

        doAnswer(invocation -> {
            Group updatedGroup = (Group) invocation.getArgument(0);
            assertEquals(requestGroupDto.getName(), updatedGroup.getName());
            return null;
        }).when(groupRepository).save(any(Group.class));

        groupService.editGroup(groupId, requestGroupDto, groupImage);

        verify(groupRepository, times(1)).save(group);
    }

    @Test
    public void shouldDeleteGroupById() throws IOException {
        Long groupId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        groupService.deleteGroupById(groupId, false);

        verify(groupRepository, times(1)).deleteByGroupId(groupId);
    }

    @Test
    public void shouldFindAllPublicGroups() {
        Group group2 = Group.builder()
                .groupId(2L)
                .name("Group 2")
                .isPublic(true)
                .groupRules(new HashSet<>())
                .groupInterests(new HashSet<>())
                .isPublic(true)
                .createdAt(LocalDateTime.now().minusDays(1L))
                .groupCreator(user)
                .groupMembers(new HashSet<>())
                .groupThreads(new HashSet<>())
                .posts(new HashSet<>())
                .build();

        List<Group> publicGroups = new ArrayList<>();
        publicGroups.add(group);
        publicGroups.add(group2);

        when(groupRepository.findByIsDeletedAndIsPublicOrderByCreatedAtDesc(false, true)).thenReturn(publicGroups);

        List<GroupDto> resultPublicGroups = groupService.findAllGroups(true);

        assertEquals(2, resultPublicGroups.size());
        assertEquals(groupDtoListMapper.convert(publicGroups), resultPublicGroups);
    }


    @Test
    public void shouldFindAllGroupsWithSimilarInterests() {
        Set<Interest> interests = new HashSet<>();
        Interest interest = Interest.builder()
                .interestId(1L)
                .name("Programowanie")
                .build();
        interests.add(interest);
        user.setUserInterests(interests);   // te same zainteresowania
        group.setGroupInterests(interests);

        List<Group> groups = new ArrayList<>();
        groups.add(group);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findByGroupInterestsInAndIsDeletedAndIsPublicOrderByCreatedAtDesc(anyList(), eq(false), eq(true)))
                .thenReturn(groups);

        List<GroupDto> resultGroups = groupService.findAllGroupsWithSimilarInterests();
        List<InterestDto> groupInterests = resultGroups.get(0).getInterests();

        assertEquals(1, resultGroups.size());
        assertSame(groupInterests.get(0).getName(), interest.getName());
    }

    @Test
    public void shouldFindGroup() {
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        GroupDetailsDto groupDetails = groupService.findGroup(groupId, false);

        assertNotNull(groupDetails);
        assertEquals("Grupa", groupDetails.getName());
    }

    @Test
    public void shouldAddGroupRuleByGroupId() {
        Long groupId = 1L;

        RequestGroupRuleDto requestGroupRuleDto = RequestGroupRuleDto.builder()
                .name("Zakaz spamowania")
                .description("Opis")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        doAnswer(invocation -> {
            GroupRule createdGroupRule = (GroupRule) invocation.getArgument(0);
            assertEquals(requestGroupRuleDto.getName(), createdGroupRule.getName());
            return null;
        }).when(groupRuleRepository).save(any(GroupRule.class));

        groupService.addGroupRuleByGroupId(groupId, requestGroupRuleDto);

        verify(groupRuleRepository, times(1)).save(any(GroupRule.class));
    }

    @Test
    public void shouldEditGroupRuleByGroupId() {
        Long groupId = 1L;
        Long ruleId = 1L;

        RequestGroupRuleDto requestGroupRuleDto = RequestGroupRuleDto.builder()
                .name("Zakaz spamowania")
                .description("Opis edytowany")  // zmiana
                .build();

        GroupRule groupRule = GroupRule.builder()
                .ruleId(1L)
                .group(group)
                .name("Zakaz spamowania")
                .description("Opis")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(groupRuleRepository.findById(ruleId)).thenReturn(Optional.of(groupRule));

        doAnswer(invocation -> {
            GroupRule updatedGroupRule = (GroupRule) invocation.getArgument(0);
            assertEquals(requestGroupRuleDto.getDescription(), updatedGroupRule.getDescription());
            return null;
        }).when(groupRuleRepository).save(any(GroupRule.class));

        groupService.editGroupRuleByGroupId(groupId, ruleId, requestGroupRuleDto);

        verify(groupRuleRepository, times(1)).save(groupRule);
    }

    @Test
    public void shouldDeleteGroupRuleByGroupId() {
        Long groupId = 1L;
        Long ruleId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        groupService.deleteGroupRuleByGroupId(groupId, ruleId);

        verify(groupRuleRepository, times(1)).deleteByRuleId(ruleId);
    }

    @Test
    public void shouldAddGroupInterest() {
        Long groupId = 1L;
        Long interestId = 1L;
        Interest interest = Interest.builder()
                .interestId(1L)
                .name("Programowanie")
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

        doAnswer(invocation -> {
            Group updatedGroup = (Group) invocation.getArgument(0);
            assertTrue(updatedGroup.getGroupInterests().contains(interest));
            return null;
        }).when(groupRepository).save(any(Group.class));

        groupService.addGroupInterest(groupId, interestId);

        verify(groupRepository, times(1)).save(group);
    }

    @Test
    public void shouldThrowErrorWhenAddGroupInterestThatExist() {
        Long groupId = 1L;
        Long interestId = 1L;
        Interest interest = Interest.builder()
                .interestId(1L)
                .name("Programowanie")
                .build();
        Set<Interest> groupInterests = new HashSet<>();
        groupInterests.add(interest);
        group.setGroupInterests(groupInterests);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

        assertThatExceptionOfType(ConflictRequestException.class)
                .isThrownBy(() -> {
                    groupService.addGroupInterest(groupId, interestId);
                }).withMessage("The given interest has already been added to the group's interests");

        verify(groupRepository, never()).save(group);
    }

    @Test
    public void shouldInviteUserToGroup() {
        Long groupId = 1L;
        Long invitedUserId = 2L;
        User user2 = new User(user);
        user2.setUserId(2L);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(userRepository.findById(invitedUserId)).thenReturn(Optional.of(user2));

        doAnswer(invocation -> {
            GroupMember createdGroupMember = (GroupMember) invocation.getArgument(0);
            assertEquals(user2, createdGroupMember.getMember());
            return null;
        }).when(groupMemberRepository).save(any(GroupMember.class));

        groupService.inviteUserToGroup(groupId, invitedUserId);

        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    public void shouldFindAllUserGroupInvitations() {
        List<GroupMember> invitedMembers = new ArrayList<>();
        User user2 = new User(user);
        user2.setUserId(2L);
        GroupMember groupMember = GroupMember.builder()
                .groupMemberId(1L)
                .groupMemberStatus(GroupMemberStatus.INVITED)
                .member(user2)
                .group(group)
                .invitationDate(LocalDateTime.now())
                .build();
        invitedMembers.add(groupMember);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupMemberRepository.findByMemberAndGroupMemberStatus(user, GroupMemberStatus.INVITED))
                .thenReturn(invitedMembers);

        List<GroupInvitationDto> groupInvitations = groupService.findAllUserGroupInvitations(false);

        assertEquals(groupInvitationDtoListMapper.convert(invitedMembers), groupInvitations);
        assertEquals(1, groupInvitations.size());
    }

    @Test
    public void shouldRespondToGroupInvitation() {
        Long groupId = 1L;
        boolean isInvitationAccepted = true;

        List<GroupMember> invitedMembers = new ArrayList<>();
        User user2 = new User(user);
        user2.setUserId(2L);
        GroupMember groupMember = GroupMember.builder()
                .groupMemberId(1L)
                .groupMemberStatus(GroupMemberStatus.INVITED)
                .member(user2)
                .group(group)
                .invitationDate(LocalDateTime.now())
                .build();
        invitedMembers.add(groupMember);
        group.setGroupMembers(new HashSet<>(invitedMembers));

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        doAnswer(invocation -> {
            GroupMember invitedMember = (GroupMember) invocation.getArgument(0);
            assertEquals(GroupMemberStatus.JOINED, invitedMember.getGroupMemberStatus());
            assertEquals(GroupPermissionType.MEMBER, invitedMember.getGroupPermissionType());
            return null;
        }).when(groupMemberRepository).save(any(GroupMember.class));

        groupService.respondToGroupInvitation(groupId, isInvitationAccepted);

        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    public void shouldAddGroupThread() {
        Long groupId = 1L;
        RequestThreadDto requestThreadDto = RequestThreadDto.builder()
                .title("Nowy Wątek")
                .content("Treść")
                .build();
        MockMultipartFile threadImage = new MockMultipartFile("image", new byte[1]);

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));

        doAnswer(invocation -> {
            GroupThread addedThread = (GroupThread) invocation.getArgument(0);
            assertEquals(requestThreadDto.getTitle(), addedThread.getTitle());
            assertEquals(requestThreadDto.getContent(), addedThread.getContent());
            return null;
        }).when(groupThreadRepository).save(any(GroupThread.class));

        groupService.addGroupThread(groupId, requestThreadDto, threadImage);

        verify(groupThreadRepository, times(1)).save(any(GroupThread.class));
    }

    @Test
    public void shouldEditGroupThread() {
        Long threadId = 1L;
        RequestThreadDto requestThreadDto = RequestThreadDto.builder()
                .title("Wątek edytowany")   // zmiana
                .content("Treść")
                .build();
        MockMultipartFile threadImage = new MockMultipartFile("image", new byte[1]);
        GroupThread savedGroupThread = GroupThread.builder()
                .threadId(1L)
                .title("Wątek")
                .content("Treść")
                .isEdited(false)
                .createdAt(LocalDateTime.now())
                .group(group)
                .threadAuthor(groupMember)
                .answers(new HashSet<>())
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupThreadRepository.findById(threadId)).thenReturn(Optional.of(savedGroupThread));

        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        doAnswer(invocation -> {
            GroupThread updatedThread = (GroupThread) invocation.getArgument(0);
            assertEquals(requestThreadDto.getTitle(), updatedThread.getTitle());
            assertTrue(updatedThread.isEdited());
            return null;
        }).when(groupThreadRepository).save(any(GroupThread.class));

        groupService.editGroupThreadById(threadId, requestThreadDto, threadImage);

        verify(groupThreadRepository, times(1)).save(any(GroupThread.class));
    }

    @Test
    public void shouldDeleteGroupThread() {
        Long threadId = 1L;

        GroupThread savedGroupThread = GroupThread.builder()
                .threadId(1L)
                .title("Wątek")
                .content("Treść")
                .isEdited(false)
                .createdAt(LocalDateTime.now())
                .group(group)
                .threadAuthor(groupMember)
                .answers(new HashSet<>())
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupThreadRepository.findById(threadId)).thenReturn(Optional.of(savedGroupThread));

        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        groupService.deleteGroupThreadById(threadId);

        verify(groupThreadRepository, times(1)).deleteByThreadId(threadId);
    }

    @Test
    public void shouldSendRequestToJoinToGroup() {
        Long groupId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        doAnswer(invocation -> {
            GroupMember notAddedMember = (GroupMember) invocation.getArgument(0);
            assertEquals(GroupMemberStatus.WANT_TO_JOIN, notAddedMember.getGroupMemberStatus());
            assertEquals(user, notAddedMember.getMember());
            return null;
        }).when(groupMemberRepository).save(any(GroupMember.class));

        groupService.wantToJoinGroup(groupId);

        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    public void shouldDecideAboutRequestToJoinToGroup() {
        User requester = new User(user);
        requester.setUserId(2L);
        Long groupId = 1L;
        Long requesterId = 2L;
        boolean isApproved = true;

        GroupMember notAddedMember = GroupMember.builder()
                .groupMemberId(2L)
                .group(group)
                .member(requester)
                .groupMemberStatus(GroupMemberStatus.WANT_TO_JOIN)
                .build();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(groupMemberRepository.findByGroupAndMember(group, requester)).thenReturn(Optional.of(notAddedMember));

        doAnswer(invocation -> {
            GroupMember addedMember = (GroupMember) invocation.getArgument(0);
            assertNotNull(addedMember.getAddedIn());
            assertEquals(GroupMemberStatus.JOINED, addedMember.getGroupMemberStatus());
            assertEquals(requester, addedMember.getMember());
            return null;
        }).when(groupMemberRepository).save(any(GroupMember.class));

        groupService.decideAboutRequestToJoin(groupId, requesterId, isApproved);

        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    public void shouldSetGroupMemberPermission() {
        User user2 = new User(user);
        user2.setUserId(2L);
        GroupMember savedGroupMember = GroupMember.builder()
                .groupMemberId(2L)
                .group(group)
                .member(user2)
                .groupMemberStatus(GroupMemberStatus.JOINED)
                .groupPermissionType(GroupPermissionType.MEMBER)
                .build();

        Long groupId = 1L;
        Long groupMemberId = 2L;
        String permission = GroupPermissionType.ASSISTANT.toString();

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndMember(group, user)).thenReturn(Optional.of(groupMember));
        when(roleRepository.findByName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(2, AppRole.ROLE_ADMIN)));

        when(groupMemberRepository.findById(groupMemberId)).thenReturn(Optional.of(savedGroupMember));

        doAnswer(invocation -> {
            GroupMember updatedMember = (GroupMember) invocation.getArgument(0);
            assertEquals(permission, updatedMember.getGroupPermissionType().toString());
            return null;
        }).when(groupMemberRepository).save(any(GroupMember.class));

        groupService.setGroupMemberPermission(groupId, groupMemberId, permission);

        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    public void shouldLeaveGroupByUser() {
        Long groupId = 1L;

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByMemberAndGroup(user, group)).thenReturn(true);

        groupService.leaveGroupByUser(groupId);

        verify(groupMemberRepository, times(1)).deleteByMemberAndGroup(user, group);
    }

    @Test
    public void shouldFindGroupThreadsById() {
        Long groupId = 1L;

        GroupThread savedGroupThread = GroupThread.builder()
                .threadId(1L)
                .title("Wątek")
                .content("Treść")
                .isEdited(false)
                .createdAt(LocalDateTime.now())
                .group(group)
                .threadAuthor(groupMember)
                .answers(new HashSet<>())
                .build();
        List<GroupThread> groupThreads = new ArrayList<>();
        groupThreads.add(savedGroupThread);
        group.setGroupThreads(new HashSet<>(groupThreads));

        when(jwtUtils.getLoggedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByMemberAndGroup(user, group)).thenReturn(true);

        List<GroupThreadDto> resultGroupThreads = groupService.findGroupThreadsById(groupId);

        assertNotNull(resultGroupThreads);
        assertEquals(groupThreadDtoListMapper.convert(groupThreads), resultGroupThreads);
    }
}

