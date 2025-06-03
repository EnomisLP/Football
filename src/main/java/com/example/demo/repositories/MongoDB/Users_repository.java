package com.example.demo.repositories.MongoDB;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.models.MongoDB.Users;
@Repository
public interface Users_repository extends MongoRepository<Users, String>{
    @Query("{ 'username' : ?0 }")
    Optional<Users> findByUsername(String username);

   
}
