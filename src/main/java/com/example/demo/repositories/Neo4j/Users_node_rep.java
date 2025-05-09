package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.UsersNodeProjection;




@Repository
public interface Users_node_rep extends Neo4jRepository<UsersNode, Long>{

    boolean existsByMongoId(String valueOf);
    Optional<UsersNode> findByMongoId(String get_id);
    Optional<UsersNode> findByUserName(String username);


    //REPOSITORIES FOR USERS INTERACTIONS

    @Query("MATCH (u:UsersNode {userName: $username})-[:FOLLOWS]->(f:UsersNode) "+
        "RETURN f { .userName, .mongoId } AS UsersNodeProjection")
    List<UsersNodeProjection> findFollowingsByUserName(String username);

    @Query("MATCH (u:UsersNode {userName: $username})<-[:FOLLOWS]-(f:UsersNode) "+
      "RETURN f { .userName, .mongoId } AS UsersNodeProjection")
    List<UsersNodeProjection> findFollowersByUserName(String username);

    @Query("MATCH (a:UsersNode {userName: $from}), (b:UsersNode {userName: $to}) "+
    "MERGE (a)-[:FOLLOWS]->(b) ")
    void createFollowRelation(@Param("from") String fromUsername, @Param("to") String toUsername);

     @Query("MATCH (a:UsersNode {userName: $from}), (b:PlayersNode {longName: $to}) "+
    "MERGE (a)-[:LIKES]->(b) ")
    void createLikeRelationToPlayer(@Param("from") String fromUsername, @Param("to") String toPlayerName);
    
    @Query("MATCH (a:UsersNode {userName: $from}), (b:CoachesNode {longName: $to}) "+
    "MERGE (a)-[:LIKES]->(b) ")
    void createLikeRelationToCoach(@Param("from") String fromUsername, @Param("to") String toCoachName);
    
    @Query("MATCH (a:UsersNode {userName: $from}), (b:TeamsNode {teamName: $to}) "+
    "MERGE (a)-[:LIKES]->(b) ")
    void createLikeRelationToTeam(@Param("from") String fromUsername, @Param("to") String toTeamsName);

    @Query("MATCH (a:UsersNode {userName: $from})-[r:FOLLOWS]->(b:UsersNode {userName: $to}) "+
    "DELETE r")
    void removeFollowingRelationship(String from, String to);
    
    @Query("MATCH (u:UsersNode {userName: $username})-[r:LIKES_TEAM|HAS_IN_M_TEAM|HAS_IN_F_TEAM|FOLLOW|FOLLOWED_BY]->(related) " +
       "RETURN u, collect(related) AS relatedNodes")
    List<UsersNode> findUserWithAllRelationships(@Param("username") String username);

  @Query("MATCH (u:UsersNode) -[r:HAS_IN_M_TEAM|HAS_IN_F_TEAM]->(p:PlayersNode ) "+
  "WHERE r.fifaVersion = $fifaV " +
  "AND p.playerId = $playerId " +
  "RETURN u")
  List<UsersNode> findUsersByPlayerIdAndFifaVersion(@Param("playerId") Integer playerId, @Param("fifaV") Integer fifaV);

}
