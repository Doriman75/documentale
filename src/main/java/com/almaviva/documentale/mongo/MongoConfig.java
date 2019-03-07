package com.almaviva.documentale.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"mongo-meta", "mongo-content"})
public class MongoConfig
{
    @Value("${documentale.mongodb.uri:localhost:27017}") String uri;
    @Value("${documentale.mongodb.meta.db:documentale}") String metaDB;
    @Value("${documentale.mongodb.meta.collection:meta}") String metaCollection;
    @Value("${documentale.mongodb.data.db:documentale}") String dataDB;
    @Value("${documentale.mongodb.data.collection:data}") String dataCollection;
    @Bean
    public MongoClient client()
    {
        return new MongoClient(uri);
    }

    @Bean @Qualifier("meta")
    public MongoCollection<Document> meta(MongoClient client)
    {
        return client.getDatabase(metaDB).getCollection(metaCollection);
    }

    
    @Bean @Qualifier("data")
    public MongoCollection<Document> data(MongoClient client)
    {
        System.out.println("dataDB " + dataDB);
        System.out.println("dataCollection " + dataCollection);
        return client.getDatabase(dataDB).getCollection(dataCollection);
    }


}