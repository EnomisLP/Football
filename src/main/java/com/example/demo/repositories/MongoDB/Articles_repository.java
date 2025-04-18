package com.example.demo.repositories.MongoDB;



import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.MongoDB.Articles;
@Repository
public interface Articles_repository extends MongoRepository<Articles,String>{

    List<Articles> findByAuthor(String username);

    
    
}
