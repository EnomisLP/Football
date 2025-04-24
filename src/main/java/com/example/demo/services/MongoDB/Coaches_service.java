package com.example.demo.services.MongoDB;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.services.Neo4j.Coaches_node_service;
import com.example.demo.requets.updateCoach;
import jakarta.transaction.Transactional;


@Service
public class Coaches_service {
    
    private  Coaches_repository CMr;
    private Coaches_node_rep Cmr;
    private Coaches_node_service CMs;
    private Teams_repository TMs;

    public Coaches_service(Coaches_repository CMr, Coaches_node_rep Cmr, Coaches_node_service CMs
    , Teams_repository TMs) {
        this.CMr = CMr;
        this.Cmr = Cmr;
        this.CMs = CMs;
        this.TMs = TMs;
    }

    
    //READ
    public Coaches getCoach(String id){
        Optional<Coaches> coach = CMr.findById(id);
        if(coach.isPresent()){
            return coach.get();
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        }
    }
    public Page<Coaches> getAllCoaches(PageRequest page, String gender){
        return CMr.findAllByGender(gender, page);
    }

    //CREATE
    @Transactional
    public Coaches createCoach(Coaches coach){
        Coaches coachM = CMr.save(coach);
        CoachesNode coachNode = new CoachesNode();
        coachNode.setMongoId(coachM.get_id());
        coachNode.setCoachId(coachM.getCoach_id());
        coachNode.setLongName(coachM.getLong_name());
        coachNode.setNationalityName(coachM.getNationality_name());
        coachNode.setGender(coachM.getGender());
        Cmr.save(coachNode);
        return coachM;
    };

     //UPDATE
     @Transactional
     public Coaches updateCoach(Integer id, updateCoach coachDetails) {
        Optional<Coaches> optionalCoach = CMr.findByCoachId(id);
        if (optionalCoach.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            Optional<CoachesNode> optionalCoachNode = Cmr.findByCoachId(id);
            if(optionalCoachNode.isPresent()){
                CoachesNode existingCoachNode = optionalCoachNode.get();
                //Checking if the coach_id was changed in the request
                if(!coachDetails.getCoach_id().equals(existingCoach.getCoach_id())){
                    //if it was changed, we need to update the coach_id in the Neo4j database 
                    //and change all coach_id instances in the teams
                    existingCoach.setCoach_id(coachDetails.getCoach_id());
                    existingCoachNode.setCoachId(coachDetails.getCoach_id());
                    List<Teams> teams = TMs.findByCoachId(id);
                    if(!teams.isEmpty()){
                        for(Teams team : teams){
                            for(FifaStatsTeam stats : team.getFifaStats()){
                                stats.setCoach_id(coachDetails.getCoach_id());
                                TMs.save(team);
                            }
                        }
                    }
                }
                // Update the other fields of the coach
                existingCoach.setLong_name(coachDetails.getLong_name());
                existingCoach.setShort_name(coachDetails.getShort_name());
                existingCoach.setGender(coachDetails.getGender());
                
                
                existingCoachNode.setLongName(coachDetails.getLong_name());
                existingCoachNode.setGender(coachDetails.getGender());

                Cmr.save(existingCoachNode);
                // Save the updated coach back to the repository
                return CMr.save(existingCoach);
                
            }    
            else{
                throw new RuntimeException("Coach with id: " + id + "is not correctly mapped into Neo4j");
            }
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    //DELETE
    @Transactional
    public void deleteCoach(Integer id){
        Optional<Coaches> coach = CMr.findByCoachId(id);
        Optional<CoachesNode> coachNode = Cmr.findByCoachId(id);
        if(coach.isPresent()){
            // Delete the coach from MongoDB
            CMr.deleteById(coach.get().get_id());
            // Delete the coach_id istances
            List<Teams> teams = TMs.findByCoachId(id);
            if(!teams.isEmpty()){
                for(Teams team : teams){
                    for(FifaStatsTeam stats : team.getFifaStats()){
                        stats.setCoach_id(null);
                        TMs.save(team);
                    }
                }
            }
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        }
        if(coachNode.isPresent()){
            CoachesNode existing = coachNode.get();
            CMs.deleteCoach(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        }
        
    }

}
