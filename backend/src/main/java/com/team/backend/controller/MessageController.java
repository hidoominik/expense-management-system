package com.team.backend.controller;

import com.team.backend.model.Message;
import com.team.backend.model.User;
import com.team.backend.model.Wallet;
import com.team.backend.service.MessageService;
import com.team.backend.service.UserService;
import com.team.backend.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    private final WalletService walletService;
    private final UserService userService;
    private final MessageService messageService;

    public MessageController(WalletService walletService, UserService userService, MessageService messageService) {
        this.walletService = walletService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping("/wallet/{id}/message")
    public ResponseEntity<?> all(@PathVariable int id) {
        Wallet wallet = walletService.findById(id).orElseThrow(RuntimeException::new);

        return new ResponseEntity<>(messageService.findAllByWalletAndTypeOrderByDate(wallet, "M"), HttpStatus.OK);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> allNotifications() {
        User user = userService.findCurrentLoggedInUser().orElseThrow(RuntimeException::new);

        return new ResponseEntity<>(messageService.findAllByReceiverAndTypeOrderByDate(user, "S"), HttpStatus.OK);
    }

    @PostMapping("/wallet/{id}/message")
    public ResponseEntity<?> createMessage(@PathVariable int id, @Valid @RequestBody Message message) {
        Wallet wallet = walletService.findById(id).orElseThrow(RuntimeException::new);
        User user = userService.findCurrentLoggedInUser().orElseThrow(RuntimeException::new);

        messageService.save(message, wallet, user);

        List<Map<String, Object>> userList = walletService.findUserList(wallet);
        for (Map<String, Object> mapUser : userList) {
            User user2 = userService.findByLogin(mapUser.get("login").toString()).orElseThrow(RuntimeException::new);

            if (user2.getId() != user.getId())
                messageService.saveNotifications(wallet, user2, user);
        }

        return ResponseEntity.ok("New message has been sent!");
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable int id) {
        Message notification = messageService.findById(id).orElseThrow(RuntimeException::new);
        messageService.delete(notification);

        return ResponseEntity.ok("Notification has been deleted!");
    }
}
