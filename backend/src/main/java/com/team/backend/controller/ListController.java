package com.team.backend.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import com.team.backend.config.ErrorMessage;
import com.team.backend.exception.ListNotFoundException;
import com.team.backend.exception.StatusNotFoundException;
import com.team.backend.exception.UserNotFoundException;
import com.team.backend.exception.WalletNotFoundException;
import com.team.backend.helpers.ListHolder;
import com.team.backend.model.*;
import com.team.backend.repository.StatusRepository;
import com.team.backend.service.ListService;
import com.team.backend.service.UserService;
import com.team.backend.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class ListController {

    private final WalletService walletService;
    private final ListService listService;
    private final StatusRepository statusRepository;
    private final UserService userService;
    private final ErrorMessage errorMessage;

    public ListController(WalletService walletService, ListService listService, StatusRepository statusRepository,
                          UserService userService, ErrorMessage errorMessage) {
        this.walletService = walletService;
        this.listService = listService;
        this.statusRepository = statusRepository;
        this.userService = userService;
        this.errorMessage = errorMessage;
    }

    @GetMapping("/shopping-list/{id}")
    @PreAuthorize("@authenticationService.isWalletMemberByShoppingList(#id)")
    public ResponseEntity<?> one(@PathVariable int id) {

        return new ResponseEntity<>(listService.findById(id).orElseThrow(ListNotFoundException::new), HttpStatus.OK);
    }

    @GetMapping("/wallet/{id}/shopping-lists")
    @PreAuthorize("@authenticationService.isWalletMember(#id)")
    public ResponseEntity<?> all(@PathVariable int id) {
        Wallet wallet = walletService.findById(id).orElseThrow(WalletNotFoundException::new);

        return new ResponseEntity<>(listService.findAllByWallet(wallet), HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/wallet/{id}/create-shopping-list")
    @PreAuthorize("@authenticationService.isWalletMember(#id)")
    public ResponseEntity<?> createList(@PathVariable int id, @Valid @RequestBody ListHolder listHolder,
                                        BindingResult bindingResult) {
        if (listService.getErrorList(bindingResult).size() != 0)
            return new ResponseEntity<>(errorMessage.get("data.error"), HttpStatus.BAD_REQUEST);

        Wallet wallet = walletService.findById(id).orElseThrow(WalletNotFoundException::new);

        listService.save(listHolder, wallet);

        return new ResponseEntity<>(listHolder.getList(), HttpStatus.OK);
    }

    @PutMapping("/shopping-list/edit/{id}")
    @PreAuthorize("@authenticationService.isWalletMemberByShoppingList(#id)")
    public ResponseEntity<?> editOne(@PathVariable int id, @RequestBody TextNode name) {
        if (name.asText().isBlank())
            return new ResponseEntity<>(errorMessage.get("shoppingList.name.notBlank"), HttpStatus.BAD_REQUEST);
        if (name.asText().length() > 45)
            return new ResponseEntity<>(errorMessage.get("shoppingList.name.size"),
                    HttpStatus.BAD_REQUEST);

        ShoppingList updatedShoppingList = listService.findById(id).orElseThrow(ListNotFoundException::new);

        updatedShoppingList.setName(name.asText());

        listService.save(updatedShoppingList);

        return new ResponseEntity<>(updatedShoppingList, HttpStatus.OK);
    }

    @PutMapping("/mobile/shopping-list/edit/{id}")
    @PreAuthorize("@authenticationService.isWalletMemberByShoppingList(#id)")
    public ResponseEntity<?> editOneMobile(@PathVariable int id, @RequestParam("name") String name) {
        if (name.isBlank())
            return new ResponseEntity<>(errorMessage.get("shoppingList.name.notBlank"), HttpStatus.BAD_REQUEST);
        if (name.length() > 45)
            return new ResponseEntity<>(errorMessage.get("shoppingList.name.size"),
                    HttpStatus.BAD_REQUEST);

        ShoppingList updatedShoppingList = listService.findById(id).orElseThrow(ListNotFoundException::new);

        updatedShoppingList.setName(name);

        listService.save(updatedShoppingList);

        return new ResponseEntity<>(updatedShoppingList, HttpStatus.OK);
    }

    @PutMapping("/change-list-status/{id}")
    @PreAuthorize("@authenticationService.isWalletMemberByShoppingList(#id)")
    public ResponseEntity<?> changeStatus(@PathVariable int id, @RequestBody int statusId) {
        ShoppingList updatedList = listService.findById(id).orElseThrow(ListNotFoundException::new);
        Status chosenStatus = statusRepository.findById(statusId).orElseThrow(StatusNotFoundException::new);
        Status pendingStatus = statusRepository.findByName("oczekujący").orElseThrow(StatusNotFoundException::new);
        Status completedStatus = statusRepository.findByName("zrealizowany").orElseThrow(StatusNotFoundException::new);
        User user = userService.findCurrentLoggedInUser().orElseThrow(UserNotFoundException::new);

        updatedList.setStatus(chosenStatus);

        if (chosenStatus.equals(pendingStatus))
            updatedList.setUser(null);
        else
            updatedList.setUser(user);

        for (ListDetail listDetail : updatedList.getListDetailSet()) {
            Status listDetailStatus = listDetail.getStatus();

            if (!listDetailStatus.equals(completedStatus)) {
                listDetail.setStatus(chosenStatus);
                if (chosenStatus.equals(pendingStatus))
                    listDetail.setUser(null);
                else
                    listDetail.setUser(user);
            }
        }

        listService.save(updatedList);

        return new ResponseEntity<>(updatedList, HttpStatus.OK);
    }

    @DeleteMapping("/shopping-list/{id}")
    @PreAuthorize("@authenticationService.isWalletMemberByShoppingList(#id)")
    public ResponseEntity<?> delete(@PathVariable int id) {
        ShoppingList shoppingList = listService.findById(id).orElseThrow(ListNotFoundException::new);

        listService.delete(shoppingList);

        return new ResponseEntity<>("The given list was deleted!", HttpStatus.OK);
    }
}
