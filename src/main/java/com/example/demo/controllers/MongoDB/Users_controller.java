package com.example.demo.controllers.MongoDB;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.MongoUserDTO;
import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.requets.ChangePasswordRequest;
import com.example.demo.requets.RegisterUserRequest;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.services.MongoDB.Users_service;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;



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
      @GetMapping("user/{_id}")
    @Operation(summary="Retrieve the informations about an user", tags={"User"})
    public MongoUserDTO getUser(@PathVariable String _id) {
        return usersService.getUser(_id);
    }

    @DeleteMapping("admin/delete/{username}")
     @Operation(summary = "DELETE operation - Delete a user by username", tags={"Admin:User"})
    public CompletableFuture<Void> deleteUserAdmin(@PathVariable String username) throws JsonProcessingException {
        return usersService.deleteUser(username);
    }

    @DeleteMapping("user/settings/deleteAccount")
     @Operation(summary = "DELETE operation - Delete a user by username", tags={"User"})
    public CompletableFuture<Void> deleteUser(Authentication auth) throws JsonProcessingException {
        return usersService.deleteUser(auth.getName());
    }

    // USER INTERACTION IN THE APP

    @PostMapping("user/article/new")
    @Operation(summary = "CREATE: create an article of a user", tags={"User:Article"})
    public CompletableFuture<Articles> createArticle(@RequestBody createArticleRequest request, Authentication auth) throws JsonProcessingException {
        return usersService.createArticle(auth.getName(), request);
    }

    @PutMapping("user/article/edit/{articleId}")
    @Operation(summary = "UPDATE: modify a specific User Article", tags={"User:Article"})
    public CompletableFuture<Articles> modifyArticle(@PathVariable String articleId,
                                  @RequestBody createArticleRequest request,
                                  Authentication auth) throws JsonProcessingException {
        return usersService.modifyArticle(auth.getName(), articleId, request);
    }

    @DeleteMapping("user/article/delete/{articleId}")
    @Operation(summary = "DELETE: delete a specific User Article", tags={"User:Article"})
    public CompletableFuture<String> deleteArticle(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
        return usersService.deleteArticle(auth.getName(), articleId);
    }
    
    @PostMapping("/signup")
    @Operation(summary = "Register a new User", tags={"Anonymous"})
    public CompletableFuture<Users> register(@RequestBody RegisterUserRequest request) throws JsonProcessingException {
        return usersService.registerUser(request.getUsername(), request.getPassword(), request.getNationality_name(), request.getE_mail());
    }

     @PostMapping("user/modify/change_password")
    @Operation(summary = "Change password", tags={"User"})
    public CompletableFuture<String> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        return usersService.changePassword(auth.getName(), request.getOldPassword(), request.getNewPassword());
    }
}
