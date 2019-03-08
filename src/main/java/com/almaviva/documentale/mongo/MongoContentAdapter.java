package com.almaviva.documentale.mongo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.almaviva.documentale.engine.ContentAdapter;
import com.almaviva.documentale.core.SecurityContext;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mongo-content")
public class MongoContentAdapter implements ContentAdapter {

    @Value("${documentale.mongodb.data.chunk_size:1000}") int chunkSize;
    @Autowired MongoCollection<Document> data;
    
    @Override
    public int create(String md5, byte[] bytes, SecurityContext sc) {
        String[] chunks = Base64
                            .getEncoder()
                            .encodeToString(bytes)
                            .split("(?<=\\G.{" + chunkSize + "})");

        if (data.countDocuments(new Document("md5", md5)) > 0) return chunks.length;

        List<Document> docs = IntStream
            .range(0, chunks.length)
            .mapToObj(i -> new Document("index", i).append("md5", md5).append("data", chunks[i]))
            .collect(Collectors.toList());

        data.insertMany(docs);
        return chunks.length;
    }

    @Override
    public byte[] bytes(String md5, SecurityContext sc) {
        List<Document> docs = new ArrayList<>();
        data
            .find(new Document("md5", md5))
            .sort(new Document("index", 1))
            .into(docs);
        return Base64
                .getDecoder()
                .decode(docs.stream().map(d -> d.getString("data"))
                .collect(Collectors.joining()));
    }
}