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
    
    @Query("{ 'fifaStats.coach_id' : ?0 }")
    List<Teams> findByCoachId(Integer coach_id);

    @Query("""
        {
          "fifaStats": {
            "$elemMatch": {
              "$or": [
                { "captain": ?0 },
                { "short_free_kick": ?0 },
                { "long_free_kick": ?0 },
                { "left_short_free_kick": ?0 },
                { "right_short_free_kick": ?0 },
                { "penalties": ?0 },
                { "left_corner": ?0 },
                { "right_corner": ?0 }
              ]
            }
          }
        }
    """)
    List<Teams> findTeamsByPlayerIdInFifaStats(Integer playerId);
}
