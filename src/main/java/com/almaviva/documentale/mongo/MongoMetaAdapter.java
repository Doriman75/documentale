package com.almaviva.documentale.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.almaviva.documentale.engine.MetaAdapter;
import com.almaviva.documentale.engine.NotFound;
import com.almaviva.documentale.interceptors.core.SecurityContext;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mongo-meta")
public class MongoMetaAdapter implements MetaAdapter {
 
    private static List<Document> versionPipe = new ArrayList<>();
    static {
        versionPipe.add(Document.parse("{ '$group': { '_id': '$id', 'documents': { '$addToSet': '$$ROOT' }, 'version': { '$max': '$version' } } }".replaceAll("'", "\"")));
        versionPipe.add(Document.parse("{ '$project': { 'document': { '$filter': { 'input': '$documents', 'as': 'e', 'cond': { '$eq': ['$$e.version', '$version'] } } } } }".replaceAll("'", "\"")));
        versionPipe.add(Document.parse("{ '$project': { 'document': { '$arrayElemAt': ['$document', 0] } } }".replaceAll("'", "\"")));
        versionPipe.add(Document.parse("{ '$replaceRoot': { newRoot: '$document' } }".replaceAll("'", "\"")));
    }

    private static Document map = new Document()
                                            .append("eq", "$eq")
                                            .append("gt", "$gt")
                                            .append("gte", "$gte")
                                            .append("lt", "$lt")
                                            .append("lte", "$lte")
                                            .append("ne", "$ne")
                                            .append("in", "$in")
                                            .append("nin", "$nin");
    

    @Autowired MongoCollection<Document> meta;

    public Map<String, Object> get(String id, int version, SecurityContext sc) {
        Document filter = new Document("id", id);
        if(version != -1) filter.append("version", version);
        Document result = meta.find(filter).sort(new Document("version", -1)).limit(1).first();
        if (result != null && result.getBoolean("deleted", false)) result = null;
        if (result == null) throw new NotFound("document [" + id + "] not found" + (version != -1 ? " with version " + version: ""));
        result.remove("_id");
        return new HashMap<String, Object>(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> find(Map<String, Object> filter, LinkedHashMap<String, Integer> sort, int offset, int limit, SecurityContext sc) {
        List<Document> pipeline = new ArrayList<>();
        filter.forEach((k,v) -> pipeline.add(new Document("$match",  new Document(k, remap(new Document((Map<String, Object>)v))))));        
        pipeline.add(new Document("$project", new Document("_id",0)));
        pipeline.addAll(versionPipe);
        pipeline.add(new Document("$match", new Document("read_groups", new Document("$in", sc.groups))));
        pipeline.add(new Document("$match", new Document("deleted", new Document("$exists", false))));
        pipeline.add(new Document("$sort", sort));
        pipeline.add(new Document("$skip", offset));
        pipeline.add(new Document("$limit", limit));

        pipeline.stream().map(Document::toJson).forEach(System.out::println);

        List<Map<String, Object>> result = new ArrayList<>();
        meta.aggregate(pipeline).into(result);
        return result;
    }

    private Document remap(Document c)
    {
        Document result = new Document();
        c.forEach((k, v)-> result.put(map.get(k, k), v));
        return result;
    }

    @Override
    public Map<String, Object> create(Map<String, Object> document, SecurityContext sc) {        
        meta.insertOne(new Document(document));
        return document;
    }

}