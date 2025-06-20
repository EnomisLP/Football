package com.example.demo.repositories.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.models.MongoDB.Coaches;


import java.util.List;

@Repository
public interface Coaches_repository extends MongoRepository<Coaches,String>{

    @Query("{ 'gender' : ?0 }")
    List<Coaches> findByGender(String gender);
    @Query("{ 'gender' : ?0 }")
    Page<Coaches> findAllByGender(String gender, PageRequest page);
       @Query("{ 'team.team_mongo_id': ?0 }")
    List<Coaches> findByTeamMongoIdInTeams(String team_mongo_id);
    
}
