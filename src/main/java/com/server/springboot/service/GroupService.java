package com.server.springboot.service;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupService {

    void addGroup(RequestGroupDto requestGroupDto, MultipartFile imageFile);

    void editGroup(Long groupId, RequestGroupDto requestGroupDto, MultipartFile imageFile);

    void deleteGroupById(Long groupId, boolean archive);

    List<GroupDto> findAllGroups(boolean isPublic);

    GroupDetailsDto findGroup(Long groupId);

    void addGroupRuleByGroupId(Long groupId, RequestGroupRuleDto requestGroupRuleDto);

    void editGroupRuleByGroupId(Long groupId, Long ruleId, RequestGroupRuleDto requestGroupRuleDto);

    void deleteGroupRuleByGroupId(Long groupId, Long ruleId);

    List<InterestDto> findAllInterests();

    void addGroupInterest(Long groupId, Long interestId);

    void deleteGroupInterest(Long groupId, Long interestId);

    void inviteUserToGroup(Long groupId, Long invitedUserId);

    List<GroupInvitationDto> findAllUserGroupInvitations(boolean isDisplayed);

    void respondToGroupInvitation(Long groupId, boolean isInvitationAccepted);

    List<PostDto> findAllGroupPostsById(Long groupId);

    void addGroupThread(Long groupId, RequestThreadDto requestThreadDto, MultipartFile imageFile);

    void editGroupThreadById(Long threadId, RequestThreadDto requestThreadDto, MultipartFile imageFile);

    void deleteGroupThreadById(Long threadId);

    void addThreadAnswer(Long threadId, RequestThreadAnswerDto requestThreadAnswerDto);

    void editThreadAnswerById(Long answerId, RequestThreadAnswerDto requestThreadAnswerDto);

    void deleteThreadAnswerById(Long answerId);

    void addThreadAnswerReview(Long answerId, RequestThreadAnswerReviewDto requestThreadAnswerReviewDto);

    void editThreadAnswerReviewById(Long reviewId, RequestThreadAnswerReviewDto requestThreadAnswerReviewDto);

    void deleteThreadAnswerReviewById(Long reviewId);

    void wantToJoinGroup(Long groupId);

    List<UserDto> findAllUserRequestToJoinGroup(Long groupId);

    void decideAboutRequestToJoin(Long groupId, Long requesterId, boolean isApproved);

    void setGroupMemberPermission(Long groupId, Long memberId, String permission);
}
