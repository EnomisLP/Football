package com.example.demo.repositories.MongoDB;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


import com.example.demo.models.MongoDB.Players;
@Repository
public interface Players_repository extends MongoRepository<Players,String>{
    @Query(value = "{ '_id': ?0, 'fifaStats.fifa_version': ?1 }", 
       fields = "{ 'fifaStats.$': 1 }") 
    Optional<Players> findPlayerWithFifaStats(String _id, Integer fifaVersion);
    @Query("{ 'gender' : ?0 }")
    List<Players> findByGender(String gender);

    @Query("{ 'gender' : ?0 }")
    Page<Players> findAllByGender(String gender, PageRequest page);
    
    @Query("""
    {
        "fifaStats": {
            "$elemMatch": {
                "team_name": ?0
            }
        }
    }
    """)
    List<Players> findByClubTeamNameInFifaStats(String team_name);

}
