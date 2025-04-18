package com.example.demo.services.MongoDB;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.ROLES;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.repositories.MongoDB.Articles_repository;
import com.example.demo.repositories.MongoDB.Users_repository;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.requets.createUserRequest;
import com.example.demo.services.Neo4j.Users_node_service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class Users_service {

    private final Users_repository Ur;
    private final Users_node_rep UNr;
    private final Users_node_service UNs;
    private final PasswordEncoder passwordEncoder;
    private final Articles_repository Ar;

    public Users_service(Users_repository ur, Users_node_rep UNR, PasswordEncoder pe,
    Articles_repository ar, Users_node_service uns){
        this.Ur = ur;
        this.UNr = UNR;
        this.passwordEncoder=pe;
        this.Ar = ar;
        this.UNs = uns;
    }
    
    public Page<Users> getAllUsers(PageRequest page){
        return Ur.findAll(page);
    }
    
    public Users getUserByUsername(String username) {
        Optional<Users> user = Ur.findByUsername(username);
        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    @Transactional
    public Users createUser(createUserRequest request) {
        Optional<Users> optionalUser = Ur.findByUsername(request.getUsername());
        if(!optionalUser.isPresent()){
            Users userNew = new Users();
            UsersNode userNode = new UsersNode();
            
            userNew.setUsername(request.getUsername());
            userNew.setRoles(request.getRoles());
            // Hash the password using BCrypt
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            userNew.setPassword(hashedPassword);
            Ur.save(userNew);

            userNode.setMongoId(userNew.get_id());
            userNode.setUserName(userNew.getUsername());
            UNr.save(userNode);
            return userNew;
        }
        else{
            throw new RuntimeException("Username already taken");
        }
        
    }
    @Transactional
    public Users updateUser(String id, Users updatedUser) {
       Optional<Users> optionalUser = Ur.findById(id);
        if (optionalUser.isPresent()) {
            Users existingUser = optionalUser.get();
            Optional<UsersNode> optionalUserNode = UNr.findByMongoId(existingUser.get_id());
            if(optionalUserNode.isPresent()){
                UsersNode existingUserNode = optionalUserNode.get();
                existingUser.setUsername(updatedUser.getUsername());
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // encodeing again if password changes
                existingUser.setRoles(updatedUser.getRoles());
                
                existingUserNode.setUserName(updatedUser.getUsername());
                UNr.save(existingUserNode);
                return Ur.save(existingUser);
            }
            else{
                throw new RuntimeException("User with id: " + id + "not correctly mapped in Neo4j");
            }
            
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }
    @Transactional
    public void deleteUser(String id) {
        Optional<Users> user = Ur.findById(id);
        Optional<UsersNode> userNode = UNr.findByMongoId(id);
        if(user.isPresent()){
            Ur.deleteById(id);
        }
        if(userNode.isPresent()){
            UsersNode existing = userNode.get();
            UNs.deleteUser(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "User not found with id: " + id);
        }
       
    }
    //ARTICLES OPERATIONS
    @Transactional
    public Articles createArticle(String username, createArticleRequest request){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        if(optionalUser.isPresent()){
            Users existiUsers = optionalUser.get();
            Articles newArticle = new Articles();
            newArticle.setAuthor(existiUsers.getUsername());
            newArticle.setTitle(request.getTitle());
            newArticle.setContent(request.getContent());
            Date publishTime = new Date();
            newArticle.setPublish_time(publishTime.toString());
            Articles savedArticle = Ar.save(newArticle);
            return savedArticle;
        }else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    public List<Articles> showUserArticles(String username){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        if(optionalUser.isPresent()){
            Users existiUsers = optionalUser.get();
            List<Articles> articles = Ar.findByAuthor(existiUsers.getUsername());
            if(articles.isEmpty()){
                throw new RuntimeErrorException(null, "User not found with username: " + username + " or no articles found for this user");
            }
            return articles;
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    public Articles showUserArticle(String username,String articleId){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        if(optionalUser.isPresent() && optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername())){
                return existingArticle;
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
    }
    @Transactional
    public Articles modifyArticle(String username, String articleId, createArticleRequest request){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        if(optionalUser.isPresent() && optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername())){
                existingArticle.setContent(request.getContent());
                existingArticle.setTitle(request.getTitle());
                Date newPublishTime = new Date();
                existingArticle.setPublish_time(newPublishTime.toString());
                return Ar.save(existingArticle);
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
        
    }
    //OPERATIONS TO MANAGE AUTHENTICATION
    @Transactional
    public Users registerUser(String username, String password) {
        createUserRequest request = new createUserRequest(
            username,
            password,
            List.of(ROLES.ROLE_USER)
        );
        return createUser(request);
    }
    @Transactional
    public String changePassword(String username,String oldPassword, String newPassword){
        Optional<Users> optional = Ur.findByUsername(username);
        if(optional.isPresent()){
            Users existing = optional.get();
            if(passwordEncoder.matches(oldPassword, existing.getPassword())){
                existing.setPassword(passwordEncoder.encode(newPassword));
                Ur.save(existing);
                return "Password Changed Correctly!";
            }
            else{
                throw new RuntimeErrorException(null, "Wrong Password, try again!");
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found in MongoDB");
        }
    }
    
    
}
