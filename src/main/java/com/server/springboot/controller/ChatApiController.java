package com.server.springboot.controller;

import com.server.springboot.domain.dto.request.RequestChatMessageDto;
import com.server.springboot.domain.dto.request.RequestChatDto;
import com.server.springboot.domain.dto.response.*;
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

    @MessageMapping("/chat/{chatId}/messages")
    public void processChatMessages(@DestinationVariable Long chatId, @Payload RequestChatMessageDto requestChatMessageDto) {
        LOGGER.info("---- Sending message to chat with id: {}", chatId);
        ChatMessageNotificationDto notificationDto = chatService.manageChatMessage(requestChatMessageDto);
        simpMessagingTemplate.convertAndSend("/topic/messages/" + chatId, notificationDto);
    }

    @PostMapping(value = "/chats/{chatId}/images", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> processChatImages(@PathVariable Long chatId,
                                               @RequestParam(value = "images") List<MultipartFile> imageFiles,
                                               @RequestParam(value = "senderId") Long senderId) {
        LOGGER.info("---- Sending images to chat with id: {}", chatId);
        chatService.saveChatImages(chatId, senderId, imageFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Create a chat")
    @PostMapping(value = "/chats", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createChat(@RequestPart(value = "image", required = false) MultipartFile imageFile,
                                        @Valid @RequestPart(value = "chat") RequestChatDto requestChatDto) {
        LOGGER.info("---- Create new chat: {}", requestChatDto.getName());
        chatService.addChat(requestChatDto, imageFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Get a private chat")
    @GetMapping(value = "/chats/private")
    public ResponseEntity<ChatDto> getPrivateChat(@RequestParam(value = "userFriendId") Long userFriendId) {
        LOGGER.info("---- Get private chat with user friend with id: {}", userFriendId);
        return new ResponseEntity<>(chatService.getPrivateChatWithFriend(userFriendId), HttpStatus.OK);
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

    @ApiOperation(value = "Get chat images")
    @GetMapping(value = "/chats/{chatId}/images")
    public ResponseEntity<List<ImageDto>> getChatImages(@PathVariable(value = "chatId") Long chatId) {
        LOGGER.info("---- Get images for chat with id: {}", chatId);
        return new ResponseEntity<>(chatService.findChatImages(chatId), HttpStatus.OK);
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

    @ApiOperation(value = "Update message on chat")
    @PutMapping(value = "/chats/messages/{messageId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateMessageOnChat(@PathVariable(value = "messageId") Long messageId,
                                                 @RequestPart(value = "image", required = false) MultipartFile image,
                                                 @RequestPart(value = "message") RequestChatMessageDto requestChatMessageDto) {
        LOGGER.info("---- Edit chat message with id: {}", messageId);
        chatService.editChatMessageById(messageId, requestChatMessageDto, image);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete message from chat with archiving")
    @DeleteMapping(value = "/chats/messages/{messageId}")
    public ResponseEntity<?> deleteMessageFromChat(@PathVariable(value = "messageId") Long messageId) {
        LOGGER.info("---- Delete chat message with id: {} with archiving", messageId);
        chatService.deleteChatMessageById(messageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Add user to chat")
    @PostMapping(value = "/chats/{chatId}/invite")
    public ResponseEntity<?> addUserToChat(@PathVariable(value = "chatId") Long chatId,
                                           @RequestParam(value = "userId") Long userId) {
        LOGGER.info("---- Add user with id: {} to chat with id: {}", userId, chatId);
        chatService.addUserToChat(chatId, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(value = "Manage chat member permission")
    @PutMapping(value = "/chats/{chatId}/permission")
    public ResponseEntity<?> manageChatMemberPermission(@PathVariable(value = "chatId") Long chatId,
                                                        @RequestParam(value = "chatMemberId") Long chatMemberId,
                                                        @RequestParam(value = "canAddMembers") boolean canAddMembers) {
        LOGGER.info("---- Chat member with id: {} access to add new members in chat: {}", chatMemberId, canAddMembers);
        chatService.manageChatMemberPermission(chatId, chatMemberId, canAddMembers);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete chat member")
    @DeleteMapping(value = "/chats/members/{chatMemberId}")
    public ResponseEntity<?> deleteChatMember(@PathVariable(value = "chatMemberId") Long chatMemberId) {
        LOGGER.info("---- Delete chat member with id: {}", chatMemberId);
        chatService.deleteChatMemberById(chatMemberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Manage member chat notifications")
    @PutMapping(value = "/chats/members/{chatMemberId}")
    public ResponseEntity<?> manageChatNotifications(@PathVariable(value = "chatMemberId") Long chatMemberId,
                                                     @RequestParam(value = "isChatMuted") boolean isChatMuted) {
        LOGGER.info("---- Manage chat notifications for member with id: {}", chatMemberId);
        chatService.manageChatMemberNotifications(chatMemberId, isChatMuted);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
