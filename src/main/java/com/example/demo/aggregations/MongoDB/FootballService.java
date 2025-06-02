package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.bson.types.ObjectId;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.demo.aggregations.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.DTO.TeamImprovements;
import com.example.demo.aggregations.DTO.monthSummary;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
public class FootballService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /*Aggregation to count number of user registered by months */
    @Async("customAsyncExecutor")
      public CompletableFuture<List<monthSummary>> getSubscriptionYearSummary(Integer year){
        
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
        return CompletableFuture.completedFuture(result.getMappedResults());
    }

    

   
    //Create the Dream Team by selecting the best players for each position
    @Async("customAsyncExecutor")
    public CompletableFuture<List<DreamTeamPlayer>> getDreamTeam(Integer fifaVersion) {
        // Define the positions to select
        List<String> dreamTeamPositions = Arrays.asList(
            "GK", "RB", "CB", "LB", "RWB",  // Defenders
            "CDM", "CM", "CAM",             // Midfielders
            "RW", "LW", "ST"                // Attackers
        );

        Aggregation aggregation = Aggregation.newAggregation(
            // Step 1: Unwind the fifaStats array to process each player's stats
            Aggregation.unwind("fifaStats"),

            // Step 2: Match based on the fifa_version
            Aggregation.match(Criteria.where("fifaStats.fifa_version").is(fifaVersion)),

            // Step 3: Split player_positions into an array
            Aggregation.addFields().addFieldWithValue("positions",
                StringOperators.Split.valueOf("fifaStats.player_positions").split(", ")).build(),

            // Step 4: Unwind the positions array
            Aggregation.unwind("positions"),

            // Step 5: Match only the desired positions
            Aggregation.match(Criteria.where("positions").in(dreamTeamPositions)),

            // Step 6: Group by position and calculate the maximum overall rating
            Aggregation.group("positions")
                .max("fifaStats.overall").as("overall")
                .first("fifaStats.player_positions").as("position")
                .first("long_name").as("playerName"),

            // Step 7: Project the final output with position, maxOverall, and player details
            Aggregation.project("position", "overall", "playerName")
        );

        // Execute the aggregation query
        AggregationResults<DreamTeamPlayer> result = mongoTemplate.aggregate(
            aggregation, "Players", DreamTeamPlayer.class
        );

        // Return the list of the best players for the selected positions
        return CompletableFuture.completedFuture(result.getMappedResults());
    }
    @Async("customAsyncExecutor")
    public CompletableFuture<TeamImprovements> getTeamImprovements(String _id,String year1,String year2){
        
        ObjectId objectId = new ObjectId(_id);
        
        //String manipulation phase
        Integer firstYear= Integer.parseInt(year1.substring(year1.length()-2,year1.length()));
        Integer secondYear= Integer.parseInt(year2.substring(year2.length()-2,year2.length()));
        
        //System.out.println(team+" "+firstYear+" "+secondYear);
        
        Aggregation aggregation = Aggregation.newAggregation(
        // Stage 1: Match
        Aggregation.match(Criteria.where("_id").is(objectId)),

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
        
        return CompletableFuture.completedFuture(result.getUniqueMappedResult());
    }
}



