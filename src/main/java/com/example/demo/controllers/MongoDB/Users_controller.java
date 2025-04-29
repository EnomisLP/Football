package com.example.demo.controllers.MongoDB;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.requets.ChangePasswordRequest;
import com.example.demo.requets.RegisterUserRequest;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.requets.createUserRequest;
import com.example.demo.services.MongoDB.Users_service;

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

    @GetMapping("/{id}")
    @Operation(summary = "READ operation - Get user by Username")
    public Users getUserById(@PathVariable String username) {
        return usersService.getUserByUsername(username);
    }

    @PostMapping("/admin")
    @Operation(summary = "CREATE operation - Create a new user")
    public Users createUser(@RequestBody createUserRequest user) {
        return usersService.createUser(user);
    }

    @PutMapping("/admin/{id}")
    @Operation(summary = "UPDATE operation - Update an existing user")
    public Users updateUser(@PathVariable String id, @RequestBody Users updatedUser) {
        return usersService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "DELETE operation - Delete a user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // USER INTERACTION IN THE APP

    @PostMapping("/user/articles")
    @Operation(summary = "CREATE: create an article of a user")
    public CompletableFuture<Articles> createArticle(@RequestBody createArticleRequest request, Authentication auth) {
        return usersService.createArticle(auth.getName(), request);
    }

    @GetMapping("/user/articles")
    @Operation(summary = "READ: show all User Articles")
    public List<Articles> getAllArticles(Authentication auth) {
        return usersService.showUserArticles(auth.getName());
    }

    @GetMapping("/user/articles/{articleId}")
    @Operation(summary = "READ: show a specific User Article")
    public Articles getArticle(@PathVariable String articleId, Authentication auth) {
        return usersService.showUserArticle(auth.getName(), articleId);
    }

    @PutMapping("/user/articles/{articleId}")
    @Operation(summary = "UPDATE: modify a specific User Article")
    public CompletableFuture<Articles> modifyArticle(@PathVariable Long articleId,
                                  @RequestBody createArticleRequest request,
                                  Authentication auth) {
        return usersService.modifyArticle(auth.getName(), auth.getName(), request);
    }

    @PostMapping("/registration")
    @Operation(summary = "Register a new User")
    public Users register(@RequestBody RegisterUserRequest request) {
        return usersService.registerUser(request.getUsername(), request.getPassword());
    }

    @PostMapping("/user/password/change")
    @Operation(summary = "Change password")
    public CompletableFuture<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        return usersService.changePassword(auth.getName(), request.getOldPassword(), request.getNewPassword());
    }
}
