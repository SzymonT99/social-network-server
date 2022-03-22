package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.request.ReqestChatMessageDto;
import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.response.ChatDetailsDto;
import com.server.springboot.domain.dto.response.ChatDto;
import com.server.springboot.domain.dto.response.ChatMessageDto;
import com.server.springboot.domain.dto.response.ChatMessageNotificationDto;
import com.server.springboot.service.ChatService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ChatApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatApiController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @Autowired
    public ChatApiController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{chatId}")
    public void processChatMessages(@DestinationVariable Long chatId, @Payload RequestChatMessageDto requestChatMessageDto) {
        LOGGER.info("---- Sending message: {} to chat with id: {}", requestChatMessageDto.getMessage(), chatId);
        ChatMessageNotificationDto notificationDto = chatService.saveMessageToChat(requestChatMessageDto);
        simpMessagingTemplate.convertAndSend("/topic/messages/" + chatId, notificationDto);
    }

    @ApiOperation(value = "Create a chat")
    @PostMapping(value = "/chats", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createChat(@RequestPart(value = "image", required = false) MultipartFile imageFile,
                                        @Valid @RequestPart(value = "chat") RequestChatDto requestChatDto) {
        LOGGER.info("---- Create new chat: {}", requestChatDto.getName());
        chatService.addChat(requestChatDto, imageFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update a chat")
    @PutMapping(value = "/chats/{chatId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateChat(@PathVariable(value = "chatId") Long chatId,
                                        @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                        @Valid @RequestPart(value = "chat") RequestChatDto requestChatDto) {
        LOGGER.info("---- Edit chat with id: {}", chatId);
        chatService.editChatById(chatId, requestChatDto, imageFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get user chats")
    @GetMapping(value = "/chats")
    public ResponseEntity<List<ChatDto>> getUserChats(@RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Get all chats for user with id: {}", userId);
        return new ResponseEntity<>(chatService.findUserChats(userId), HttpStatus.OK);
    }

    @ApiOperation(value = "Get chat details by id")
    @GetMapping(value = "/chats/{chatId}")
    public ResponseEntity<ChatDetailsDto> getChatById(@PathVariable(value = "chatId") Long chatId) {
        LOGGER.info("---- Get chat with id: {}", chatId);
        return new ResponseEntity<>(chatService.findChatById(chatId), HttpStatus.OK);
    }

    @ApiOperation(value = "Delete a chat")
    @DeleteMapping(value = "/chats/{chatId}")
    public ResponseEntity<?> deleteChat(@PathVariable(value = "chatId") Long chatId) {
        LOGGER.info("Delete chat with id: {}", chatId);
        chatService.deleteChatById(chatId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Get chat message by id")
    @GetMapping(value = "/chats/messages/{messageId}")
    public ResponseEntity<ChatMessageDto> getChatMessageById(@PathVariable(value = "messageId") Long messageId) {
        LOGGER.info("---- Get chat message with id: {}", messageId);
        return new ResponseEntity<>(chatService.findChatMessageById(messageId), HttpStatus.OK);
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
