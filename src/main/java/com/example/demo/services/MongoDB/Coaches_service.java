package com.example.demo.services.MongoDB;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import com.example.demo.models.MongoDB.Coaches;

import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.services.Neo4j.Coaches_node_service;

import jakarta.transaction.Transactional;


@Service
public class Coaches_service {
    @Autowired
    private  Coaches_repository CMr;
    private Coaches_node_rep Cmr;
    private Coaches_node_service CMs;

    
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
     public Coaches updateCoach(String id, Coaches coachDetails) {
        Optional<Coaches> optionalCoach = CMr.findById(id);
        if (optionalCoach.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            Optional<CoachesNode> optionalCoachNode = Cmr.findByMongoId(id);
            if(optionalCoachNode.isPresent()){
                CoachesNode existingCoachNode = optionalCoachNode.get();
                existingCoach.setCoach_id(coachDetails.getCoach_id());
                existingCoach.setLong_name(coachDetails.getLong_name());
                existingCoach.setShort_name(coachDetails.getShort_name());
                existingCoach.setNationality_name(coachDetails.getNationality_name());
                existingCoach.setGender(coachDetails.getGender());
            
                existingCoachNode.setCoachId(coachDetails.getCoach_id());
                existingCoachNode.setLongName(coachDetails.getLong_name());
                existingCoachNode.setNationalityName(coachDetails.getNationality_name());
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
    public void deleteCoach(String id){
        Optional<Coaches> coach = CMr.findById(id);
        Optional<CoachesNode> coachNode = Cmr.findByMongoId(id);
        if(coach.isPresent()){
            CMr.deleteById(id);
        }
        if(coachNode.isPresent()){
            CoachesNode existing = coachNode.get();
            CMs.deleteCoach(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        }
        
    };
}
