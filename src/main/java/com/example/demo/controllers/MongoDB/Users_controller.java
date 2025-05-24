package com.example.demo.controllers.MongoDB;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.requets.ChangePasswordRequest;
import com.example.demo.requets.RegisterUserRequest;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.services.MongoDB.Users_service;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/Users")
@Tag(name = "Users", description = "CRUD operations for Users")
public class Users_controller {

    @Autowired
    private Users_service usersService;

    @GetMapping("/admin")
    @Operation(summary = "READ: get all Users")
    public <Pageable> Page<Users> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return usersService.getAllUsers(pageable);
    }

    @GetMapping("/{username}")
    @Operation(summary = "READ operation - Get user by Username")
    public Users getUserByUsername(@PathVariable String username) {
        return usersService.getUserByUsername(username);
    }

    @PutMapping("/user/{oldUserName}/{newUserName}")
    @Operation(summary = "UPDATE operation - Update an existing user")
    public CompletableFuture<Users> updateUserName(@PathVariable String oldUserName, @PathVariable String newUserName) throws JsonProcessingException {
        return usersService.updateUserName(oldUserName, newUserName);
    }

    @DeleteMapping("/admin/{username}")
    @Operation(summary = "DELETE operation - Delete a user by username")
    public CompletableFuture<Void> deleteUser(@PathVariable String username) throws JsonProcessingException {
        return usersService.deleteUser(username);
    }

    // USER INTERACTION IN THE APP

    @PostMapping("/user/articles")
    @Operation(summary = "CREATE: create an article of a user")
    public CompletableFuture<Articles> createArticle(@RequestBody createArticleRequest request, Authentication auth) throws JsonProcessingException {
        return usersService.createArticle(auth.getName(), request);
    }

    @PutMapping("/user/articles/{articleId}")
    @Operation(summary = "UPDATE: modify a specific User Article")
    public CompletableFuture<Articles> modifyArticle(@PathVariable String articleId,
                                  @RequestBody createArticleRequest request,
                                  Authentication auth) throws JsonProcessingException {
        return usersService.modifyArticle(auth.getName(), articleId, request);
    }

    @DeleteMapping("/user/articles/{articleId}")
    @Operation(summary = "DELETE: delete a specific User Article")
    public CompletableFuture<String> deleteArticle(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
        return usersService.deleteArticle(auth.getName(), articleId);
    }
    
    @PostMapping("/registration")
    @Operation(summary = "Register a new User")
    public CompletableFuture<Users> register(@RequestBody RegisterUserRequest request) throws JsonProcessingException {
        return usersService.registerUser(request.getUsername(), request.getPassword());
    }

    @PostMapping("/user/password/change")
    @Operation(summary = "Change password")
    public CompletableFuture<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        return usersService.changePassword(auth.getName(), request.getOldPassword(), request.getNewPassword());
    }
}
