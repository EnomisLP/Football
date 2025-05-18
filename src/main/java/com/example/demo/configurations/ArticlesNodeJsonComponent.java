package com.example.demo.configurations;

import com.example.demo.models.Neo4j.ArticlesNode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class ArticlesNodeJsonComponent {

    public static class ArticlesNodeSerializer extends JsonSerializer<ArticlesNode> {

        @Override
        public void serialize(ArticlesNode article, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("mongoId", article.getMongoId());
            gen.writeStringField("title", article.getTitle());
            gen.writeStringField("author", article.getAuthor()); // Assuming it's a simple String
            gen.writeEndObject();
        }
    }

    public static class ArticlesNodeDeserializer extends JsonDeserializer<ArticlesNode> {

        @Override
        public ArticlesNode deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = parser.getCodec();
            JsonNode tree = codec.readTree(parser);

            String mongoId = tree.has("mongoId") ? tree.get("mongoId").asText() : null;
            String title = tree.has("title") ? tree.get("title").asText() : null;
            String author = tree.has("author") ? tree.get("author").asText() : null;

            ArticlesNode article = new ArticlesNode();
            article.setMongoId(mongoId);
            article.setTitle(title);
            article.setAuthor(author);

            return article;
        }
    }
}
