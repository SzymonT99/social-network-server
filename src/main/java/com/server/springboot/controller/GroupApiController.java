package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.*;
import com.server.springboot.domain.dto.response.*;
import io.swagger.annotations.ApiOperation;
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
public class GroupApiController {

    @ApiOperation(value = "Create a group")
    @PostMapping(value = "/groups", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroup(@RequestPart(value = "image") MultipartFile imageFile,
                                         @Valid @RequestPart(value = "group") RequestGroupDto requestGroupDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group by id")
    @PutMapping(value = "/groups/{groupId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateGroup(@PathVariable(value = "groupId") Long groupId,
                                         @RequestPart(value = "image") MultipartFile imageFile,
                                         @Valid @RequestPart(value = "group") RequestGroupDto requestGroupDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a group with archiving")
    @DeleteMapping(value = "/groups/{groupId}/archive")
    public ResponseEntity<?> deleteGroup(@PathVariable(value = "groupId") Long groupId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all groups")
    @GetMapping(value = "/groups")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        List<GroupDto> groupDtoList = new ArrayList<>();
        return new ResponseEntity<>(groupDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get group detail by id")
    @GetMapping(value = "/groups/{groupId}")
    public ResponseEntity<GroupDetailsDto> getGroupDetails(@PathVariable(value = "groupId") Long groupId) {
        return new ResponseEntity<>(new GroupDetailsDto(), HttpStatus.OK);
    }

    @ApiOperation(value = "Create group rule")
    @PostMapping(value = "/groups/{groupId}/rules")
    public ResponseEntity<?> createGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @Valid @RequestBody RequestGroupRule requestGroupRule) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group rule")
    @PutMapping(value = "/groups/{groupId}/rules")
    public ResponseEntity<?> updateGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @Valid @RequestBody RequestGroupRule requestGroupRule) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete group rule")
    @DeleteMapping(value = "/groups/{groupId}/rules/{ruleId}")
    public ResponseEntity<?> deleteGroupRules(@PathVariable(value = "groupId") Long groupId,
                                              @PathVariable(value = "ruleId") Long ruleId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all possible group interests")
    @GetMapping(value = "/groups/interests")
    public ResponseEntity<List<InterestDto>> getAllPossibleInterests() {
        List<InterestDto> interestDtoList = new ArrayList<>();
        return new ResponseEntity<>(interestDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get all possible group interests")
    @PutMapping(value = "/groups/{groupId}/interests/{interestId}")
    public ResponseEntity<?> chooseGroupInterest(@PathVariable(value = "groupId") Long groupId,
                                                 @PathVariable(value = "interestId") Long interestId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Invite user to group")
    @PostMapping(value = "/group/{groupId}/invite")
    public ResponseEntity<?> inviteForGroup(@PathVariable(value = "groupId") Long eventId,
                                            @RequestParam(value = "userId") Long userId) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get user's group invitations")
    @GetMapping(value = "/groups/invitations")
    public ResponseEntity<List<GroupInvitationDto>> getUserInvitationToGroups(@RequestParam(value = "userId") Long userId) {
        List<GroupInvitationDto> groupInvitationDtoList = new ArrayList<>();
        return new ResponseEntity<>(groupInvitationDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "Respond to group invitation")
    @PutMapping(value = "/groups/{groupId}")
    public ResponseEntity<?> respondToGroupInvitation(@PathVariable(value = "groupId") Long eventId,
                                                      @RequestParam(value = "invitationAccept") boolean invitationAccept) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group post")
    @PostMapping(value = "/groups/{groupId}/posts", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroupPost(@PathVariable(value = "groupId") Long groupId,
                                             @RequestPart(value = "images") List<MultipartFile> imageFiles,
                                             @Valid @RequestPart(value = "post") RequestPostDto requestPostDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update existing group post by id")
    @PutMapping(value = "/groups/{groupId}/posts/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateGroupPost(@PathVariable(value = "groupId") Long groupId,
                                             @PathVariable(value = "postId") Long postId,
                                             @RequestPart(value = "images") List<MultipartFile> imageFiles,
                                             @Valid @RequestPart(value = "poxt") RequestPostDto requestPostDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all group posts")
    @GetMapping(value = "/group/{groupId}/posts")
    public ResponseEntity<List<PostDto>> getAllGroupPosts(@PathVariable(value = "groupId") Long groupId) {
        List<PostDto> postDtoList = new ArrayList<>();
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "Create a group thread")
    @PostMapping(value = "/groups/{groupId}/threads", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createGroupThread(@PathVariable(value = "groupId") Long groupId,
                                               @RequestPart(value = "image") MultipartFile imageFile,
                                               @Valid @RequestPart(value = "thread") RequestThreadDto requestThreadDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a group thread")
    @PutMapping(value = "/groups/{groupId}/threads/{threadId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateGroupThread(@PathVariable(value = "groupId") Long groupId,
                                               @PathVariable(value = "threadId") Long threadId,
                                               @RequestPart(value = "image") MultipartFile imageFile,
                                               @Valid @RequestPart(value = "thread") RequestThreadDto requestThreadDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete a group thread by id with archiving")
    @DeleteMapping(value = "/groups/{groupId}/threads/{threadId}/archive")
    public ResponseEntity<?> deleteThreadWithArchiving(@PathVariable(value = "groupId") Long postId,
                                                       @PathVariable(value = "threadId") Long threadId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
