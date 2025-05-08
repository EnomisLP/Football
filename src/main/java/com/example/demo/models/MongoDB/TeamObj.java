package com.example.demo.models.MongoDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class TeamObj {
    String team_mongo_id;
    String team_name;
    Long team_id;
    Integer fifa_version;
}
