package com.example.demo.models.Neo4j;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Node(labels = "ArticlesNode")
public class ArticlesNode {
    @Id
    @GeneratedValue
    private Long _id;

    @Property(name = "mongoId")
    @Indexed(unique = true)
    private String mongoId;
    @Property(name = "author")
    @Indexed
    private String author;
    @Property(name = "title")
    private String title;
    
    @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> usersNode = new ArrayList<>();
    @JsonIgnore
    @Relationship(type = "WROTE", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> wroteNode = new ArrayList<>();
}
