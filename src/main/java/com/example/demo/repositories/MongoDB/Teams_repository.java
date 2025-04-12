package com.example.demo.repositories.MongoDB;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


import com.example.demo.models.MongoDB.Teams;
@Repository
public interface Teams_repository extends MongoRepository<Teams,String>{
    @Query("{ 'team_id' : ?0 }")
    Optional<Teams> findByTeamId(Long team_id);
    @Query("{ 'gender' : ?0 }")
    List<Teams> findByGender(String gender);

    @Query("{ 'gender' : ?0 }")
    Page<Teams> findAllByGender(String gender, PageRequest page);
    
}
