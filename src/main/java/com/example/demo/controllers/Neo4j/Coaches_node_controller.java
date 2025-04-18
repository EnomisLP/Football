package com.example.demo.controllers.Neo4j;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.services.Neo4j.Coaches_node_service;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/api/v1/Coaches_Node")
@Tag(name = "Coaches_node", description = "QUERIES AND AGGREGATION FOR COACHES_NODE")
public class Coaches_node_controller{

  
    private Coaches_node_service coachesMNodeService;

    public Coaches_node_controller (Coaches_node_service CMNS){
        this.coachesMNodeService = CMNS;
    }

    // READ: Get coaches nodes
    @GetMapping("/{id}")
    @Operation(summary = "READ operation: Get coaches nodes")
    public CoachesNode getCoach(@PathVariable Integer id) {
        return coachesMNodeService.getCoach(id);
    }

    @GetMapping("/admin/user/byGender/{gender}")
    @Operation(summary = "READ: Get all coaches by gender with pagination")
    public Page<CoachesNode> getAllCoaches(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, 
                                       @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return coachesMNodeService.getAllCoaches(pageable, gender);
    }

    @GetMapping("/{coachId1}/history")
    @Operation(summary = "show team trained Hystory")
    public List<manages_team> showTrainedHistory(@PathVariable Integer coachId1) {
        return coachesMNodeService.showTrainedHistory(coachId1);
    }
    
    @GetMapping("/{coachId}/CurrentTeam")
    @Operation(summary = "show current team training")
    public manages_team showCurrenTraining(@PathVariable Integer coachId) {
        return coachesMNodeService.showCurrentTeam(coachId);
    }

    @GetMapping("/{coachId}/{fifaV}")
    @Operation(summary = "show specific team training")
    public manages_team showSpecificTraining(@PathVariable Integer coachId, @PathVariable Integer fifaV) {
        return coachesMNodeService.showSpecificTeam(coachId, fifaV);
    }

    // MAP: Map all Coaches from MongoDB to Neo4j nodes
    @PostMapping("/admin/map-all")
    @Operation(summary = "MAP operation: Map all Coaches from MongoDB to Neo4j nodes")
    public String mapAllNodes() {
        return coachesMNodeService.MapAllTheNodes();
    }
    @PostMapping("/admin/{gender}")
    @Operation(summary = "MAP all MANAGES_TEAMS relationships")
    public String mapManagesTeam(@PathVariable String gender){
        return coachesMNodeService.MapAllManagesTeam(gender);
    }
}

