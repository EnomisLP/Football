package com.example.demo.configurations;

import com.example.demo.models.Neo4j.CoachesNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class CoachesNodeJsonComponent {

    public static class CoachesNodeSerializer extends JsonSerializer<CoachesNode> {
        @Override
        public void serialize(CoachesNode coach, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("id", coach.get_id());
            gen.writeStringField("mongoId", coach.getMongoId());
            gen.writeStringField("longName", coach.getLongName());
            gen.writeStringField("gender", coach.getGender());
            gen.writeStringField("nationalityName", coach.getNationalityName());
            gen.writeEndObject();
        }
    }

    public static class CoachesNodeDeserializer extends JsonDeserializer<CoachesNode> {
        @Override
        public CoachesNode deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = parser.getCodec();
            JsonNode node = codec.readTree(parser);

            CoachesNode coach = new CoachesNode();
            if (node.has("id")) coach.set_id(node.get("id").asLong());
            if (node.has("mongoId")) coach.setMongoId(node.get("mongoId").asText());
            if (node.has("longName")) coach.setLongName(node.get("longName").asText());
            if (node.has("gender")) coach.setGender(node.get("gender").asText());
            if (node.has("nationalityName")) coach.setNationalityName(node.get("nationalityName").asText());

            return coach;
        }
    }
}
