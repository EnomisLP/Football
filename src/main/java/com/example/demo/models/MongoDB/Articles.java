package com.example.demo.models.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection ="Articles")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Data
public class Articles {
    @Id
    @GeneratedValue
    private String _id;
    @Field(name ="author")
    private String author;
    @Field(name ="content")
    private String content;
    @Field(name = "publish_time")
    private String publish_time;
    @Field(name = "tilte")
    private String title;

    
}
