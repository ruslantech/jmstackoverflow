package com.javamentor.qa.platform.webapp.controllers;

import com.javamentor.qa.platform.models.dto.MessageDto;
import com.javamentor.qa.platform.models.dto.ChatDto;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.SingleChatDto;
import com.javamentor.qa.platform.models.entity.chat.Chat;
import com.javamentor.qa.platform.service.abstracts.dto.MessageDtoService;
import com.javamentor.qa.platform.models.entity.chat.Message;
import com.javamentor.qa.platform.security.util.SecurityHelper;
import com.javamentor.qa.platform.service.abstracts.dto.ChatDtoService;
import com.javamentor.qa.platform.service.abstracts.dto.SingleChatDtoService;
import com.javamentor.qa.platform.service.abstracts.model.ChatService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/chat")
@Api(value = "ChatApi")
public class ChatController {

    private final SecurityHelper securityHelper;
    private final ChatDtoService chatDtoService;
    private final ChatService chatService;
    private final SingleChatDtoService singleChatDtoService;
    private final MessageDtoService messageDtoService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private static final int MAX_ITEMS_ON_PAGE = 100;

    @Autowired
    public ChatController(SingleChatDtoService singleChatDtoService, SecurityHelper securityHelper, ChatDtoService chatDtoService, ChatService chatService, MessageDtoService messageDtoService, SimpMessagingTemplate simpMessagingTemplate) {
        this.singleChatDtoService = singleChatDtoService;
        this.securityHelper = securityHelper;
        this.chatDtoService = chatDtoService;
        this.chatService = chatService;
        this.messageDtoService = messageDtoService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @GetMapping(path = "/single")
    @ApiOperation(value = "Get page SingleChatDto. MAX ITEMS ON PAGE=" + MAX_ITEMS_ON_PAGE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<SingleChatDto>"),
    })
    public ResponseEntity<?> getAllSingleChatPagination(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size
    ) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<SingleChatDto, Object> allSingleChats = singleChatDtoService.getAllSingleChatDtoPagination(page, size);

        return ResponseEntity.ok(allSingleChats);
    }

    @GetMapping(path = "/{chatId}/message")
    @ApiOperation(value = "Get page MessageDto. MAX ITEMS ON PAGE=" + MAX_ITEMS_ON_PAGE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<MessageDto> "),
    })
    public ResponseEntity<?> getAllMessageByChatIdPagination(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size,

            @PathVariable("chatId") long chatId
    ) {
        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        PageDto<MessageDto, Object> allMessage = messageDtoService.getAllMessageDtoByChatIdPagination(page, size, chatId);
        List<Object> id = new ArrayList<>();
        id.add(securityHelper.getPrincipal().getId());
        allMessage.setMeta(id);
        return ResponseEntity.ok(allMessage);
    }




    @MessageMapping("/message")
    public Message proceedMessage(Map<String, String> message) throws Exception {
        String messageText = message.get("message");
        Long chatId = Long.parseLong(message.get("chatId"));

        System.out.println(chatId);
        System.out.println(messageText);

        Message messageEntity = new Message();
        messageEntity.setMessage(messageText);

        if (chatId != null) {
            Optional<Chat> chatOptional = chatService.getById(chatId);
            Chat chat = chatOptional.get();

            messageEntity.setChat(chat);
        }

        simpMessagingTemplate.convertAndSend("/" + chatId +"/message", message);
        return messageEntity;
    }

    @GetMapping(path = "/byuser")
    @ApiOperation(value = "Get Chats By User. MAX ITEMS ON PAGE=" + MAX_ITEMS_ON_PAGE)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returns the pagination List<ChatDto>"),
    })
    public ResponseEntity<?> getAllChatsByUserPagination(
            @ApiParam(name = "page", value = "Number Page. type int", required = true, example = "1")
            @RequestParam("page") int page,
            @ApiParam(name = "size", value = "Number of entries per page.Type int." +
                    " Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE,
                    example = "10")
            @RequestParam("size") int size
    ) {

        if (page <= 0 || size <= 0 || size > MAX_ITEMS_ON_PAGE) {
            return ResponseEntity.badRequest().body("Номер страницы и размер должны быть " +
                    "положительными. Максимальное количество записей на странице " + MAX_ITEMS_ON_PAGE);
        }

        Long userId = securityHelper.getPrincipal().getId();

        PageDto<ChatDto, Object> allChatsByUser = chatDtoService.getAllChatsByUserPagination(userId, page, size);

        return ResponseEntity.ok(allChatsByUser);

    }

}
