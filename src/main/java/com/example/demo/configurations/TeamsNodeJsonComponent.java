package com.example.demo.configurations;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.example.demo.models.Neo4j.TeamsNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonComponent
public class TeamsNodeJsonComponent {
     public static class TeamsNodeSerializer extends JsonSerializer<TeamsNode> {
        @Override
        public void serialize(TeamsNode team, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("mongoId", team.getMongoId());
            gen.writeStringField("longName", team.getLongName());
            gen.writeStringField("gender", team.getGender());
            gen.writeEndObject();
        }
    }

    public static class TeamsNodeDeserializer extends JsonDeserializer<TeamsNode> {
        @Override
        public TeamsNode deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = parser.getCodec();
            JsonNode node = codec.readTree(parser);

            TeamsNode team = new TeamsNode();
            team.setMongoId(node.get("mongoId").asText());
            team.setLongName(node.get("longName").asText());
            team.setGender(node.get("gender").asText());
            return team;
        }
    }
}

