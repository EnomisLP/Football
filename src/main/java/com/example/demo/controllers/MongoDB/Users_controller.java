package com.example.demo.controllers.MongoDB;
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
import com.example.demo.services.MongoDB.Users_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/")

public class Users_controller {

    @Autowired
    private Users_service usersService;

    @GetMapping("admin/users")
    @Operation(summary = "READ: get all Users", tags={"Admin:User"})
    public <Pageable> Page<Users> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return usersService.getAllUsers(pageable);
    }

    @GetMapping("admin/user/usernameSearch/{username}")
    @Operation(summary = "READ operation - Get user by Username", tags={"Admin:User"})
    public Users getUserById(@PathVariable String username) {
        return usersService.getUserByUsername(username);
    }

    @PutMapping("admin/user/modify/{id}")
    @Operation(summary = "UPDATE operation - Update an existing user", tags={"Admin:User"})
    public Users updateUser(@PathVariable String id, @RequestBody Users updatedUser) {
        return usersService.updateUser(id, updatedUser);
    }

    @DeleteMapping("admin/user/delete/{id}")
    @Operation(summary = "DELETE operation - Delete a user by ID", tags={"Admin:User"})
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // USER INTERACTION IN THE APP

    @PostMapping("article/new_article")
    @Operation(summary = "CREATE: create an article of a user", tags={"Article"})
    public CompletableFuture<Articles> createArticle(@RequestBody createArticleRequest request, Authentication auth) {
        return usersService.createArticle(auth.getName(), request);
    }

    @PutMapping("article/edit/{articleId}")
    @Operation(summary = "UPDATE: modify a specific User Article", tags={"Article"})
    public CompletableFuture<Articles> modifyArticle(@PathVariable String articleId,
                                  @RequestBody createArticleRequest request,
                                  Authentication auth) {
        return usersService.modifyArticle(auth.getName(), articleId, request);
    }

    @DeleteMapping("article/delete/{articleId}")
    @Operation(summary = "DELETE: delete a specific User Article", tags={"Article"})
    public CompletableFuture<String> deleteArticle(@PathVariable String articleId, Authentication auth) {
        return usersService.deleteArticle(auth.getName(), articleId);
    }
    
    @PostMapping("/signup")
    @Operation(summary = "Register a new User", tags={"Anonymous"})
    public Users register(@RequestBody RegisterUserRequest request) {
        return usersService.registerUser(request.getUsername(), request.getPassword());
    }

    @PostMapping("user/change_password")
    @Operation(summary = "Change password", tags={"User"})
    public CompletableFuture<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        return usersService.changePassword(auth.getName(), request.getOldPassword(), request.getNewPassword());
    }
}
