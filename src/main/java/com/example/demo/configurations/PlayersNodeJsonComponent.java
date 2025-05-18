package com.example.demo.configurations;

import com.example.demo.models.Neo4j.PlayersNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class PlayersNodeJsonComponent {

    public static class PlayersNodeSerializer extends JsonSerializer<PlayersNode> {
        @Override
        public void serialize(PlayersNode player, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("id", player.get_id());
            gen.writeStringField("mongoId", player.getMongoId());
            gen.writeStringField("longName", player.getLongName());
            gen.writeStringField("gender", player.getGender());
            gen.writeNumberField("age", player.getAge());
            gen.writeStringField("nationalityName", player.getNationalityName());
            gen.writeEndObject();
        }
    }

    public static class PlayersNodeDeserializer extends JsonDeserializer<PlayersNode> {
        @Override
        public PlayersNode deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = parser.getCodec();
            JsonNode node = codec.readTree(parser);

            PlayersNode player = new PlayersNode();
            if (node.has("id")) player.set_id(node.get("id").asLong());
            if (node.has("mongoId")) player.setMongoId(node.get("mongoId").asText());
            if (node.has("longName")) player.setLongName(node.get("longName").asText());
            if (node.has("gender")) player.setGender(node.get("gender").asText());
            if (node.has("age")) player.setAge(node.get("age").asInt());
            if (node.has("nationalityName")) player.setNationalityName(node.get("nationalityName").asText());

            return player;
        }
    }
}
