package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import com.server.springboot.service.GroupService;
import com.server.springboot.service.PostService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class GroupApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final GroupService groupService;
    private final PostService postService;

    @Autowired
    public GroupApiController(GroupService groupService, PostService postService) {
        this.groupService = groupService;
        this.postService = postService;
    }

    @ApiOperation(value = "Create a group")
    @PostMapping(value = "/groups", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroup(@RequestPart(value = "image", required = false) MultipartFile imageFile,
                                         @Valid @RequestPart(value = "group") RequestGroupDto requestGroupDto) {
        LOGGER.info("---- Create group with name: {}", requestGroupDto.getName());
        groupService.addGroup(requestGroupDto, imageFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group by id")
    @PutMapping(value = "/groups/{groupId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateGroup(@PathVariable(value = "groupId") Long groupId,
                                         @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                         @Valid @RequestPart(value = "group") RequestGroupDto requestGroupDto) throws IOException {
        LOGGER.info("---- Update group with id: {} and name: {}", groupId, requestGroupDto.getName());
        groupService.editGroup(groupId, requestGroupDto, imageFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a group by id")
    @DeleteMapping(value = "/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable(value = "groupId") Long groupId,
                                         @RequestParam(value = "archive") boolean archive) throws IOException {
        LOGGER.info("---- Delete group with id: {}", groupId);
        groupService.deleteGroupById(groupId, archive);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all public groups")
    @GetMapping(value = "/groups")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        LOGGER.info("---- Get all public groups");
        return new ResponseEntity<>(groupService.findAllGroups(true), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all groups with similar interests")
    @GetMapping(value = "/interesting-groups")
    public ResponseEntity<List<GroupDto>> getAllGroupsWithSimilarInterests() {
        LOGGER.info("---- Get all groups with similar interests");
        return new ResponseEntity<>(groupService.findAllGroupsWithSimilarInterests(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all user groups")
    @GetMapping(value = "/users/{userId}/groups")
    public ResponseEntity<List<GroupDto>> getAllUserGroups(@PathVariable(value = "userId") Long userId) {
        LOGGER.info("---- Get all user groups");
        return new ResponseEntity<>(groupService.findAllUserGroups(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get group details by id")
    @GetMapping(value = "/groups/{groupId}")
    public ResponseEntity<GroupDetailsDto> getGroupDetails(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get group details with id: {}", groupId);
        return new ResponseEntity<>(groupService.findGroup(groupId, false), HttpStatus.OK);
    }

    @ApiOperation(value = "Get public group details by id")
    @GetMapping(value = "/public/groups/{groupId}")
    public ResponseEntity<GroupDetailsDto> getPublicGroupDetails(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get group details with id: {}", groupId);
        return new ResponseEntity<>(groupService.findGroup(groupId, true), HttpStatus.OK);
    }

    @ApiOperation(value = "Create group rule")
    @PostMapping(value = "/groups/{groupId}/rules")
    public ResponseEntity<?> createGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @Valid @RequestBody RequestGroupRuleDto requestGroupRuleDto) {
        LOGGER.info("---- Create group rule for group with id: {}", groupId);
        groupService.addGroupRuleByGroupId(groupId, requestGroupRuleDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group rule")
    @PutMapping(value = "/groups/{groupId}/rules/{ruleId}")
    public ResponseEntity<?> updateGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @PathVariable(value = "ruleId") Long ruleId,
                                              @Valid @RequestBody RequestGroupRuleDto requestGroupRuleDto) {
        LOGGER.info("---- Update group rule for group with id: {}", groupId);
        groupService.editGroupRuleByGroupId(groupId, ruleId, requestGroupRuleDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete group rule")
    @DeleteMapping(value = "/groups/{groupId}/rules/{ruleId}")
    public ResponseEntity<?> deleteGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @PathVariable(value = "ruleId") Long ruleId) {
        LOGGER.info("---- Update group rule for group with id: {}", groupId);
        groupService.deleteGroupRuleByGroupId(groupId, ruleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add group interest")
    @PostMapping(value = "/groups/{groupId}/interests/{interestId}")
    public ResponseEntity<?> addGroupInterest(@PathVariable(value = "groupId") Long groupId,
                                              @PathVariable(value = "interestId") Long interestId) {
        LOGGER.info("---- Add group interest for group with id: {}", groupId);
        groupService.addGroupInterest(groupId, interestId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete group interest")
    @DeleteMapping(value = "/groups/{groupId}/interests/{interestId}")
    public ResponseEntity<?> deleteGroupInterest(@PathVariable(value = "groupId") Long groupId,
                                                 @PathVariable(value = "interestId") Long interestId) {
        LOGGER.info("---- Delete group interest for group with id: {}", groupId);
        groupService.deleteGroupInterest(groupId, interestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Send request to join the group")
    @PostMapping(value = "/groups/{groupId}/request")
    public ResponseEntity<?> wantToJoinGroup(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- User sends a request to join the group with id: {}", groupId);
        groupService.wantToJoinGroup(groupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Invite user to group")
    @PostMapping(value = "/groups/{groupId}/invite")
    public ResponseEntity<?> inviteForGroup(@PathVariable(value = "groupId") Long groupId,
                                            @RequestParam(value = "invitedUserId") Long invitedUserId) {
        LOGGER.info("---- Invite user with id: {} to group with id: {}", invitedUserId, groupId);
        groupService.inviteUserToGroup(groupId, invitedUserId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get user's group invitations")
    @GetMapping(value = "/groups/invitations")
    public ResponseEntity<List<GroupInvitationDto>> getUserInvitationsToGroup() {
        LOGGER.info("---- Get all user group invitations");
        return new ResponseEntity<>(groupService.findAllUserGroupInvitations(false), HttpStatus.OK);
    }

    @ApiOperation(value = "Get users who want to join the group")
    @GetMapping(value = "/groups/{groupId}/requests")
    public ResponseEntity<List<UserDto>> getUserRequestsToJoinGroup(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get user's requests to join the group");
        return new ResponseEntity<>(groupService.findAllUserRequestToJoinGroup(groupId), HttpStatus.OK);
    }

    @ApiOperation(value = "Decide about user group requests to join")
    @PutMapping(value = "/groups/{groupId}/requests")
    public ResponseEntity<?> decideAboutRequestToJoin(@PathVariable(value = "groupId") Long groupId,
                                                      @RequestParam(value = "requesterId") Long requesterId,
                                                      @RequestParam(value = "isApproved") boolean isApproved) {
        LOGGER.info("---- Decide about request to join the group with id: {} for user with id: {}", groupId, requesterId);
        groupService.decideAboutRequestToJoin(groupId, requesterId, isApproved);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Respond to group invitation")
    @PutMapping(value = "/groups/{groupId}/response")
    public ResponseEntity<?> respondToGroupInvitation(@PathVariable(value = "groupId") Long groupId,
                                                      @RequestParam(value = "isInvitationAccepted") boolean isInvitationAccepted) {
        LOGGER.info("---- User reactions to a group invitation: {}", isInvitationAccepted);
        groupService.respondToGroupInvitation(groupId, isInvitationAccepted);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "User leaves the group")
    @DeleteMapping(value = "/groups/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- User leave the group with id: {}", groupId);
        groupService.leaveGroupByUser(groupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete group member")
    @DeleteMapping(value = "/groups/{groupId}/members/{memberId}")
    public ResponseEntity<?> deleteGroupMember(@PathVariable(value = "groupId") Long groupId,
                                               @PathVariable(value = "memberId") Long memberId) {
        LOGGER.info("---- Delete member with id: {} from group with id: {}", memberId, groupId);
        groupService.deleteGroupMemberById(memberId, groupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group post")
    @PostMapping(value = "/groups/{groupId}/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroupPost(@PathVariable(value = "groupId") Long groupId,
                                             @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                             @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Create post for group with id: {}", groupId);
        postService.addPost(requestPostDto, imageFiles, groupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group post by id")
    @PutMapping(value = "/groups/posts/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<PostDto> updateGroupPost(@PathVariable(value = "postId") Long postId,
                                                   @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                                   @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        LOGGER.info("---- Update post with id: {} in group", postId);
        return new ResponseEntity<>(postService.editPost(postId, requestPostDto, imageFiles), HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a group post by id")
    @DeleteMapping(value = "/groups/posts/{postId}")
    public ResponseEntity<?> deleteGroupPost(@PathVariable(value = "postId") Long postId) {
        LOGGER.info("---- Delete group post with id: {}", postId);
        postService.deletePostById(postId, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all group posts")
    @GetMapping(value = "/group/{groupId}/posts")
    public ResponseEntity<List<PostDto>> getAllGroupPosts(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get all posts in group with id: {}", groupId);
        return new ResponseEntity<>(groupService.findAllGroupPostsById(groupId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get group forum")
    @GetMapping(value = "/groups/{groupId}/forum")
    public ResponseEntity<List<GroupThreadDto>> getGroupForum(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get forum for group with id: {}", groupId);
        return new ResponseEntity<>(groupService.findGroupThreadsById(groupId), HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group thread")
    @PostMapping(value = "/groups/{groupId}/threads", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroupThread(@PathVariable(value = "groupId") Long groupId,
                                               @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                               @Valid @RequestPart(value = "thread") RequestThreadDto requestThreadDto) {
        LOGGER.info("---- Create thread in group with id: {}", groupId);
        groupService.addGroupThread(groupId, requestThreadDto, imageFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a group thread")
    @PutMapping(value = "/groups/threads/{threadId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateGroupThread(@PathVariable(value = "threadId") Long threadId,
                                               @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                               @Valid @RequestPart(value = "thread") RequestThreadDto requestThreadDto) {
        LOGGER.info("---- Update group thread with id: {}", threadId);
        groupService.editGroupThreadById(threadId, requestThreadDto, imageFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a group thread")
    @DeleteMapping(value = "/groups/threads/{threadId}")
    public ResponseEntity<?> deleteThread(@PathVariable(value = "threadId") Long threadId) {
        LOGGER.info("---- Delete a group thread with id: {}", threadId);
        groupService.deleteGroupThreadById(threadId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group thread answer")
    @PostMapping(value = "/groups/threads/{threadId}/answers")
    public ResponseEntity<?> createThreadAnswer(@PathVariable(value = "threadId") Long threadId,
                                                @Valid @RequestBody RequestThreadAnswerDto requestThreadAnswerDto) {
        LOGGER.info("---- Create answer to group thread with id: {}", threadId);
        groupService.addThreadAnswer(threadId, requestThreadAnswerDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a group thread answer")
    @PutMapping(value = "/groups/threads/answers/{answerId}")
    public ResponseEntity<?> updateThreadAnswer(@PathVariable(value = "answerId") Long answerId,
                                                @Valid @RequestBody RequestThreadAnswerDto requestThreadAnswerDto) {
        LOGGER.info("---- Update answer with id: {} in group thread", answerId);
        groupService.editThreadAnswerById(answerId, requestThreadAnswerDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a group thread answer")
    @DeleteMapping(value = "/groups/threads/answers/{answerId}")
    public ResponseEntity<?> deleteThreadAnswer(@PathVariable(value = "answerId") Long answerId) {
        LOGGER.info("---- Delete answer with id: {} in group thread", answerId);
        groupService.deleteThreadAnswerById(answerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group thread answer review")
    @PostMapping(value = "/groups/threads/answers/{answerId}/reviews")
    public ResponseEntity<?> createThreadAnswerReview(@PathVariable(value = "answerId") Long answerId,
                                                      @Valid @RequestBody RequestThreadAnswerReviewDto requestThreadAnswerReviewDto) {
        LOGGER.info("---- Create review to group thread answer with id: {}", answerId);
        groupService.addThreadAnswerReview(answerId, requestThreadAnswerReviewDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a group thread answer review")
    @PutMapping(value = "/groups/threads/answers/reviews/{reviewId}")
    public ResponseEntity<?> updateThreadAnswerReview(@PathVariable(value = "reviewId") Long reviewId,
                                                      @Valid @RequestBody RequestThreadAnswerReviewDto requestThreadAnswerReviewDto) {
        LOGGER.info("---- Update review with id: {} in group thread answer", reviewId);
        groupService.editThreadAnswerReviewById(reviewId, requestThreadAnswerReviewDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Set group member permission")
    @PutMapping(value = "/groups/{groupId}/members/{memberId}")
    public ResponseEntity<?> setMemberPermission(@PathVariable(value = "groupId") Long groupId,
                                                 @PathVariable(value = "memberId") Long memberId,
                                                 @RequestParam(value = "permission") String permission) {
        LOGGER.info("---- Set group member with id: {} permission type: {}", memberId, permission);
        groupService.setGroupMemberPermission(groupId, memberId, permission);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get group members forum stats")
    @GetMapping(value = "/groups/{groupId}/forum/stats")
    public ResponseEntity<List<GroupMemberForumStatsDto>> setMemberPermission(@PathVariable(value = "groupId") Long groupId) {
        LOGGER.info("---- Get members forum stats for group with id: {}", groupId);
        return new ResponseEntity<>(groupService.getGroupForumStatsById(groupId), HttpStatus.OK);
    }
}
