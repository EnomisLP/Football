package com.example.demo.models.MongoDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TeamObj {
    String team_mongo_id;
    String team_name;
    Integer fifa_version;
}
