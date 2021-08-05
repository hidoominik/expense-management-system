package com.team.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.backend.helpers.WalletHolder;
import com.team.backend.model.User;
import com.team.backend.model.UserStatus;
import com.team.backend.model.Wallet;
import com.team.backend.model.WalletUser;
import com.team.backend.repository.UserStatusRepository;
import com.team.backend.service.UserService;
import com.team.backend.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;
    private final UserStatusRepository userStatusRepository;
    private final ObjectMapper objectMapper;

    public WalletController(WalletService walletService, UserService userService,
                            UserStatusRepository userStatusRepository, ObjectMapper objectMapper) {
        this.walletService = walletService;
        this.userService = userService;
        this.userStatusRepository = userStatusRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/wallet/{id}")
    public ResponseEntity<?> one(@PathVariable int id) {
        Wallet wallet = walletService.findById(id)
                .orElseThrow(RuntimeException::new);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("walletId", wallet.getId());
        objectNode.put("name", wallet.getName());
        objectNode.put("description", wallet.getDescription());
        objectNode.put("owner", walletService.findOwner(wallet).getLogin());
        objectNode.put("userListCounter", walletService.findUserList(wallet).size());

        return new ResponseEntity<>(objectNode, HttpStatus.OK);
    }

    @GetMapping("/wallets")
    public ResponseEntity<?> all() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserLogin = authentication.getName();

        User user = userService.findByLogin(currentUserLogin).orElseThrow(RuntimeException::new);
        List<Wallet> wallets = walletService.findWallets(user);

        List<ObjectNode> walletsList = new ArrayList<>();

        for (Wallet wallet : wallets) {
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("walletId", wallet.getId());
            objectNode.put("name", wallet.getName());
            objectNode.put("owner", walletService.findOwner(wallet).getLogin());
            objectNode.put("userListCounter", walletService.findUserList(wallet).size());

            walletsList.add(objectNode);
        }

        return new ResponseEntity<>(walletsList, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/create-wallet")
    public ResponseEntity<?> createWallet(@Valid @RequestBody WalletHolder walletHolder) {
        walletService.save(walletHolder);

        return new ResponseEntity<>(walletHolder.getWallet().getId(), HttpStatus.OK);
    }

    @PutMapping("/wallet/{id}")
    public ResponseEntity<?> editOne(@PathVariable int id, @RequestBody Wallet newWallet) {
        Wallet updatedWallet = walletService.findById(id).orElseThrow(RuntimeException::new);

        updatedWallet.setName(newWallet.getName());
        updatedWallet.setDescription(newWallet.getDescription());
        updatedWallet.setWalletCategory(newWallet.getWalletCategory());

        walletService.save(updatedWallet);

        return new ResponseEntity<>(updatedWallet.getId(), HttpStatus.OK);
    }

    @PutMapping("/wallet/{id}/users/{userLogin}")
    public ResponseEntity<?> addUsers(@PathVariable int id, @PathVariable String userLogin) {
        Wallet updatedWallet = walletService.findById(id).orElseThrow(RuntimeException::new);

        for (WalletUser walletUser : updatedWallet.getWalletUserSet()) {
            if (walletUser.getUser().getLogin().equals(userLogin))
                return new ResponseEntity<>("Person already exists for login " + userLogin + " in this wallet!", HttpStatus.CONFLICT);
        }

        UserStatus waitingStatus = userStatusRepository.findById(2).orElseThrow(RuntimeException::new);

        walletService.saveUser(userLogin, updatedWallet, waitingStatus);

        walletService.save(updatedWallet);

        return new ResponseEntity<>(updatedWallet.getId(), HttpStatus.OK);
    }
}
