package com.server.springboot.controller;

import com.server.springboot.domain.dto.response.FriendDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class FriendApiController {

    @ApiOperation(value = "Invite user to friends")
    @PostMapping(value = "/friends")
    public ResponseEntity<?> inviteToFriends(@RequestParam(value = "userId") Long userId) {

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Respond to the invitation to friends")
    @PutMapping(value = "/friends/{inviterId}/response")
    public ResponseEntity<?> respondToFriendInvitation(@PathVariable(value = "inviterId") Long inviterId,
                                            @RequestParam(value = "reaction") String reactionToInvitation) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete user from friends")
    @DeleteMapping(value = "/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable(value = "friendId") Long friendId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get all user's friends")
    @GetMapping(value = "/friends")
    public ResponseEntity<List<FriendDto>> getAllUserFriends(@RequestParam(value = "userId") Long userId) {
        List<FriendDto> friendDtoList = new ArrayList<>();
        return new ResponseEntity<>(friendDtoList, HttpStatus.OK);
    }
}
