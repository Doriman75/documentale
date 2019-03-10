package com.almaviva.documentale.mongo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.almaviva.documentale.NotFound;
import com.almaviva.documentale.core.Doc;
import com.almaviva.documentale.core.Page;
import com.almaviva.documentale.core.SecurityContext;
import com.almaviva.documentale.engine.MetaAdapter;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mongo-meta")
public class MongoMetaAdapter implements MetaAdapter {
 
    private static List<Document> VERSION_PIPE = new ArrayList<>();
    static {
        VERSION_PIPE.add(Document.parse("{'$group':{'_id':'$id','documents':{'$addToSet':'$$ROOT'},'version':{'$max':'$version'}}}".replaceAll("'", "\"")));
        VERSION_PIPE.add(Document.parse("{'$project':{'document':{'$filter':{'input':'$documents','as':'e','cond':{'$eq':['$$e.version', '$version']}}}}}".replaceAll("'", "\"")));
        VERSION_PIPE.add(Document.parse("{'$project':{'document':{'$arrayElemAt':['$document',0]}}}".replaceAll("'", "\"")));
        VERSION_PIPE.add(Document.parse("{'$replaceRoot':{newRoot:'$document'}}".replaceAll("'", "\"")));
    }

    private static Document OPERATOR_MAP = new Document()
                                            .append("eq", "$eq")
                                            .append("gt", "$gt")
                                            .append("gte", "$gte")
                                            .append("lt", "$lt")
                                            .append("lte", "$lte")
                                            .append("ne", "$ne")
                                            .append("in", "$in")
                                            .append("nin", "$nin");
    

    @Autowired MongoCollection<Document> meta;
    
    @SuppressWarnings("unchecked")
    public Doc get(String id, int version, SecurityContext sc) {
        Document filter = new Document("id", id).append("deleted", new Document("$exists", false));
        if(version != -1) filter.append("version", version);
        Document result = meta.find(filter).sort(new Document("version", -1)).limit(1).first();
        if (result == null) throw new NotFound("document [" + id + "] not found" + (version != -1 ? " with version " + version: ""));        
        result.remove("_id");
        sc.check(result.get("read_groups", List.class));
        return new Doc(result);
    }

    @Override
    public Page find(Doc filter, LinkedHashMap<String, Integer> sort, int offset, int limit, SecurityContext sc) {
        Page page = new Page();
        page.limit = limit;
        page.offset = offset;        
        page.list = new ArrayList<>();

        List<Document> basePipeline = pipeline(filter, sc);
        page.count = doCount(basePipeline);
        if (page.count == 0) return page;
        List<Document> list = doFind(sort, offset, limit, basePipeline);
        page.list = list.stream().map(e -> new Doc(e)).collect(Collectors.toList());
        return page;
    }

    private List<Document> pipeline(Doc filter, SecurityContext sc) {
        List<Document> pipeline = new ArrayList<>();
        filter.forEach((k, v) -> pipeline.add(new Document("$match", new Document(k, remap(new Document((LinkedHashMap) v))))));
        pipeline.add(new Document("$match", new Document("read_groups", new Document("$in", sc.groups))));
        pipeline.add(new Document("$match", new Document("deleted", new Document("$exists", false))));
        pipeline.add(new Document("$project", new Document("_id", 0)));
        pipeline.addAll(VERSION_PIPE);
        return pipeline;
    }

    private int doCount(List<Document> basePipeline)
    {
        List<Document> pipeline = new ArrayList<>(basePipeline);
        pipeline.add(new Document("$count", "count"));
        pipeline.stream().map(Document::toJson).forEach(System.out::println);
        return meta.aggregate(pipeline).first().getInteger("count");
    }

    private List<Document> doFind(LinkedHashMap<String, Integer> sort, int offset, int limit, List<Document> basePipeline) {
        List<Document> pipeline = new ArrayList<>(basePipeline);
        pipeline.add(new Document("$sort", sort));
        pipeline.add(new Document("$skip", offset));
        pipeline.add(new Document("$limit", limit));
        pipeline.stream().map(Document::toJson).forEach(System.out::println);
        List<Document> list = new ArrayList<>();
        meta.aggregate(pipeline).into(list);
        return list;
    }
    
    @Override
    public Doc create(Doc document, SecurityContext sc) {        
        meta.insertOne(new Document(document));
        return document;
    }
    
    private Document remap(Document c)
    {
        Document result = new Document();
        c.forEach((k, v)-> result.put(OPERATOR_MAP.get(k, k), v));
        return result;
    }
}