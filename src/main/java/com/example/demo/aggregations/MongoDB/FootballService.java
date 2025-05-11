package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.stereotype.Service;

import com.example.demo.aggregations.DTO.ClubAverage;
import com.example.demo.aggregations.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.DTO.TopPlayersByCoach;
import com.example.demo.aggregations.DTO.*;

import java.util.Arrays;
import java.util.List;


@Service
public class FootballService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Return the top 10 clubs by average overall rating,
     * grouped by club name     */
    public List<ClubAverage> getTopClubsByAverageOverall() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("fifaStats"),
                // Group by team_name, compute average and max club worth
                Aggregation.group("team_name")
                        .avg("fifaStats.overall").as("averageOverall")
                        .avg("fifaStats.club_worth_eur").as("maxClubWorth"),
                Aggregation.project("averageOverall", "maxClubWorth")
                        .and("_id").as("clubName"), // pull team name from _id, i.e. the group key
                Aggregation.sort(Sort.by(
                        Sort.Order.desc("averageOverall"),
                        Sort.Order.desc("maxClubWorth")
                )),
                Aggregation.limit(10)
        );
    
        AggregationResults<ClubAverage> results = mongoTemplate.aggregate(
                aggregation, "Teams", ClubAverage.class
        );
    
        return results.getMappedResults();
    }
    
    
    

    /**
     * Return the top 10 best players managed by a specific coach,
     * including the FIFA version of the corresponding stat.
     */
    public List<TopPlayersByCoach> getTopPlayersManagedByCoach(int coachId) {
        Aggregation aggregation = Aggregation.newAggregation(
            // Step 1: Unwind fifaStats to filter by coach
            Aggregation.unwind("fifaStats"),
    
            // Step 2: Match only entries with the given coach
            Aggregation.match(Criteria.where("fifaStats.coach_id").is(coachId)),
    
            // Step 3: Create player_ids array from role fields
            Aggregation.project("fifaStats.fifa_version", "fifaStats.coach_id", "fifaStats.team_name")
                .and(ArrayOperators.ConcatArrays.arrayOf(
                    Arrays.asList(
                        Fields.field("fifaStats.short_free_kick"),
                        Fields.field("fifaStats.long_free_kick"),
                        Fields.field("fifaStats.left_short_free_kick"),
                        Fields.field("fifaStats.right_short_free_kick"),
                        Fields.field("fifaStats.penalties"),
                        Fields.field("fifaStats.left_corner"),
                        Fields.field("fifaStats.right_corner"),
                        Fields.field("fifaStats.captain")
                    )
                )).as("player_ids")
                .and("fifaStats.fifa_version").as("fifa_version")
                .and("fifaStats.team_name").as("team_name"),
    
            // Step 4: Unwind player_ids to access each player
            Aggregation.unwind("player_ids"),
    
            // Step 5: Lookup in Players collection
            Aggregation.lookup("Players", "player_ids", "player_id", "player"),
    
            Aggregation.unwind("player"),
            Aggregation.unwind("player.fifaStats"),
    
            // Step 6: Create flattened result for scoring
            Aggregation.project()
                .and("player.long_name").as("playerName")
                .and("player.fifaStats.overall").as("overall")
                .and("player.fifaStats.fifa_version").as("fifaVersion")
                .and("player.fifaStats.club_name").as("teamName")
                .and("player.player_id").as("playerId"),
    
            // Step 7: Group by playerId to get max overall per player
            Aggregation.group("playerId")
                .first("playerName").as("playerName")
                .first("fifaVersion").as("fifaVersion")
                .first("teamName").as("teamName")
                .max("overall").as("overall"),
    
            // Step 8: Sort & Limit
            Aggregation.sort(Sort.by(Sort.Order.desc("overall"))),
            Aggregation.limit(10)
        );
    
        AggregationResults<TopPlayersByCoach> results = mongoTemplate.aggregate(
            aggregation, "Teams", TopPlayersByCoach.class
        );
        return results.getMappedResults();
    }
    //Create the Dream Team by selecting the best players for each position
    public List<DreamTeamPlayer> getDreamTeam(Integer fifaVersion) {
        // Define the positions to select 
        List<String> dreamTeamPositions = Arrays.asList(
        "GK", "RB", "CB", "LB", "RWB",  // Defenders
        "CDM", "CM", "CAM",      // Midfielders
        "RW", "LW", "ST"         // Attackers
    );
    
        Aggregation aggregation = Aggregation.newAggregation(
            // Step 1: Unwind the fifaStats array to process each player's stats
            Aggregation.unwind("fifaStats"),
    
            // Step 2: Match based on the fifa_version and only include players with the required positions
            Aggregation.match(Criteria.where("fifaStats.fifa_version").is(fifaVersion)
                    .and("fifaStats.player_positions").in(dreamTeamPositions)),
    
            // Step 3: Project the necessary fields for each player (name, position, overall, player_id, fifa_version)
            Aggregation.project("long_name")
                .and("fifaStats.player_positions").as("position")
                .and("fifaStats.overall").as("overall")
                .and("fifaStats.fifa_version").as("fifaVersion")
                .and("player_id").as("playerId"),
    
            // Step 4: Sort by overall rating in descending order (so best players come first)
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "overall")),
    
            // Step 5: Group by position and select the best player for each position
            Aggregation.group("position")
                .first("long_name").as("playerName")
                .first("overall").as("overall")
                .first("playerId").as("playerId")
                .first("fifaVersion").as("fifaVersion")
                .first("position").as("position"),
    
            // Step 6: Project the final output with player name, overall, position, and fifaVersion
            Aggregation.project("playerName", "overall", "fifaVersion", "position")
        );
    
        // Execute the aggregation query
        AggregationResults<DreamTeamPlayer> result = mongoTemplate.aggregate(
            aggregation, "Players", DreamTeamPlayer.class
        );
    
        // Return the list of the best players for the selected positions
        return result.getMappedResults();
    }
    
    public List<monthSummary> getSubscriptionYearSummary(Integer year){
        
        Aggregation aggregation = Aggregation.newAggregation(
            //step 1 match by year
            Aggregation.match(Criteria.where("signup_date").regex(year.toString())),
                   
            //step 2 add field and split the string date
            AddFieldsOperation.builder()
                .addField("dateTokens")
                .withValue(StringOperators.Split.valueOf("signup_date").split("/"))
                .build(),
            
            //create a new field which is the signup month
            AddFieldsOperation.builder()
            .addField("signup_month")
            .withValue(ArrayOperators.ArrayElemAt.arrayOf("dateTokens").elementAt(1))
            .build(),
                   
            //stage 3 group by month and count new subscribers
            Aggregation.group("signup_month").count().as("new_users"),
            
            //sort by month
            Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
           );
                   
        // Execute the aggregation query
        AggregationResults<monthSummary> result = mongoTemplate.aggregate(
            aggregation, "Users", monthSummary.class
        );
    
        // Return the list of the best players for the selected positions
        return result.getMappedResults();
    }
    
    public TeamImprovements getTeamImprovements(String team,String year1,String year2){
        
        //String manipulation phase
        Integer firstYear= Integer.parseInt(year1.substring(year1.length()-2,year1.length()));
        Integer secondYear= Integer.parseInt(year2.substring(year2.length()-2,year2.length()));
        
        System.out.println(team+" "+firstYear+" "+secondYear);
        
        Aggregation aggregation = Aggregation.newAggregation(
        // Stage 1: Match
        Aggregation.match(Criteria.where("team_name").is("FC Barcelona")),

        // Stage 2: Project filtered stats for FIFA 24 and 23
        Aggregation.project()
            .and(ArrayOperators.ArrayElemAt.arrayOf(
                ArrayOperators.Filter.filter("fifaStats")
                    .as("st")
                    .by(ComparisonOperators.Eq.valueOf("$$st.fifa_version").equalToValue(secondYear))
            ).elementAt(0)).as("first_stats")
            .and(ArrayOperators.ArrayElemAt.arrayOf(
                ArrayOperators.Filter.filter("fifaStats")
                    .as("st")
                    .by(ComparisonOperators.Eq.valueOf("$$st.fifa_version").equalToValue(firstYear))
            ).elementAt(0)).as("second_stats"),

        // Stage 3: Project improvement percentages
        Aggregation.project()
            .andExclude("_id")
            .and(
                ArithmeticOperators.Multiply.valueOf(
                    ArithmeticOperators.Divide.valueOf(
                        ArithmeticOperators.Subtract.valueOf("second_stats.attack")
                            .subtract("first_stats.attack")
                    ).divideBy("first_stats.attack")
                ).multiplyBy(100)
            ).as("attack_improvement")
            .and(
                ArithmeticOperators.Multiply.valueOf(
                    ArithmeticOperators.Divide.valueOf(
                        ArithmeticOperators.Subtract.valueOf("second_stats.midfield")
                            .subtract("first_stats.midfield")
                    ).divideBy("first_stats.midfield")
                ).multiplyBy(100)
            ).as("midfield_improvement")
            .and(
                ArithmeticOperators.Multiply.valueOf(
                    ArithmeticOperators.Divide.valueOf(
                        ArithmeticOperators.Subtract.valueOf("second_stats.defence")
                            .subtract("first_stats.defence")
                    ).divideBy("first_stats.defence")
                ).multiplyBy(100)
            ).as("defence_improvement")
    );

        // Run the aggregation
        AggregationResults<TeamImprovements> result = mongoTemplate.aggregate(
            aggregation,"Teams",TeamImprovements.class);
        
        return result.getUniqueMappedResult();
    }
       
}



