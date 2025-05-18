package com.example.demo.configurations;

import com.example.demo.models.Neo4j.UsersNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class UsersNodeJsonComponent {

    public static class UsersNodeSerializer extends JsonSerializer<UsersNode> {

        @Override
        public void serialize(UsersNode user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("id", user.get_id());
            gen.writeStringField("mongoId", user.getMongoId());
            gen.writeStringField("userName", user.getUserName());
            // You can choose what to expose. Relationships are ignored to avoid cycles.
            gen.writeEndObject();
        }
    }

    public static class UsersNodeDeserializer extends JsonDeserializer<UsersNode> {

        @Override
        public UsersNode deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = parser.getCodec();
            JsonNode tree = codec.readTree(parser);

            Long id = tree.has("id") ? tree.get("id").asLong() : null;
            String mongoId = tree.has("mongoId") ? tree.get("mongoId").asText() : null;
            String userName = tree.has("userName") ? tree.get("userName").asText() : null;

            UsersNode user = new UsersNode();
            user.set_id(id);
            user.setMongoId(mongoId);
            user.setUserName(userName);

            // Do NOT populate relationships here, as they're typically managed by Neo4j
            return user;
        }
    }
}

