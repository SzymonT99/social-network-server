package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.ReqestChatMessageDto;
import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.response.ChatDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ChatApiController {

    @ApiOperation(value = "Create a chat")
    @PostMapping(value = "/chats")
    public ResponseEntity<?> createChat(@Valid @RequestBody RequestChatDto requestChatDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a chat by id")
    @PostMapping(value = "/chats/{chatId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable(value = "chatId") Long chatId) {
        return new ResponseEntity<>(new ChatDto(), HttpStatus.OK);
    }

    @ApiOperation(value = "Update a chat")
    @PutMapping(value = "/chats")
    public ResponseEntity<?> updateChat(@Valid @RequestBody RequestChatDto requestChatDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a chat with archiving")
    @DeleteMapping(value = "/chats/{chatId}")
    public ResponseEntity<?> deleteChat(@PathVariable(value = "chatId") Long chatId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Invite user to chat")
    @PostMapping(value = "/chats/{chatId}/invite")
    public ResponseEntity<?> inviteToChat(@PathVariable(value = "chatId") Long chatId,
                                          @RequestParam(value = "userId") Long userId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Send message on chat")
    @PostMapping(value = "/chats/{chatId}/messages", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> sendMessageOnChat(@PathVariable(value = "chatId") Long chatId,
                                               @RequestPart(value = "image") MultipartFile image,
                                               @RequestPart(value = "chat") ReqestChatMessageDto reqestChatMessageDto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update message on chat")
    @PutMapping(value = "/chats/{chatId}/messages/{messageId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateMessageOnChat(@PathVariable(value = "chatId") Long chatId,
                                                   @PathVariable(value = "messageId") Long messageId,
                                                   @RequestPart(value = "image") MultipartFile image,
                                                   @RequestPart(value = "chat") ReqestChatMessageDto reqestChatMessageDto) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete message from chat with archiving")
    @DeleteMapping(value = "/chats/{chatId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessageFromChat(@PathVariable(value = "chatId") Long chatId,
                                                   @PathVariable(value = "messageId") Long messageId) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
