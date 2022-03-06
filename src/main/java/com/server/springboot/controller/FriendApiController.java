package com.server.springboot.controller;

import com.server.springboot.domain.dto.response.FriendDto;
import com.server.springboot.domain.dto.response.FriendInvitationDto;
import com.server.springboot.domain.dto.response.FriendSuggestionDto;
import com.server.springboot.domain.dto.response.SentFriendInvitationDto;
import com.server.springboot.service.FriendService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class FriendApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final FriendService friendService;

    @Autowired
    public FriendApiController(FriendService friendService) {
        this.friendService = friendService;
    }

    @ApiOperation(value = "Invite user to friends")
    @PostMapping(value = "/friends/invitations")
    public ResponseEntity<?> inviteToFriends(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("User invites to friends user with id: {}", userId);
        friendService.inviteToFriendsByUserId(userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get all received invitations to friends")
    @GetMapping(value = "/friends/invitations/received")
    public ResponseEntity<List<FriendInvitationDto>> getUserReceivedInvitationToFriends(@RequestParam(value = "userId") Long userId,
                                                                                @RequestParam(value = "isDisplayed") boolean isDisplayed) {
        LOGGER.info("---- Get all received invitation to friends");
        return new ResponseEntity<>(friendService.findAllUserReceivedInvitationsToFriends(userId, isDisplayed), HttpStatus.OK);
    }

    @ApiOperation(value = "Get all sent invitations to friends")
    @GetMapping(value = "/friends/invitations/sent")
    public ResponseEntity<List<SentFriendInvitationDto>> getUserSentInvitationToFriends(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all sent invitation to friends");
        return new ResponseEntity<>(friendService.findAllUserSentInvitationsToFriends(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Respond to the invitation to friends")
    @PutMapping(value = "/friends/{inviterId}/response")
    public ResponseEntity<?> respondToFriendInvitation(@PathVariable(value = "inviterId") Long inviterId,
                                                       @RequestParam(value = "reaction") String reactionToInvitation) {
        LOGGER.info("---- User responds to the invitation of the inviter (id: {}) with a reaction: {}",
                inviterId, reactionToInvitation);
        friendService.respondToFriendInvitation(inviterId, reactionToInvitation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user from friends / Delete invitation to friends")
    @DeleteMapping(value = "/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable(value = "friendId") Long friendId,
                                          @RequestParam(value = "isDeletedInvitation") boolean isDeletedInvitation) {
        LOGGER.info("---- User deletes friend | invitation to friends for friend id: {}", friendId);
        friendService.deleteFriendById(friendId, isDeletedInvitation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all user's friends")
    @GetMapping(value = "/friends")
    public ResponseEntity<List<FriendDto>> getAllUserFriends(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- User with id: {} get all friend", userId);
        return new ResponseEntity<>(friendService.findAllUserFriends(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get friends suggestions")
    @GetMapping(value = "/friends/suggestions")
    public ResponseEntity<List<FriendSuggestionDto>> getFriendsSuggestions() {
        LOGGER.info("---- User get suggestions of friends");
        return new ResponseEntity<>(friendService.findAllFriendsSuggestions(), HttpStatus.OK);
    }
}
