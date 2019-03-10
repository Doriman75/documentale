package com.almaviva.documentale.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.almaviva.documentale.BadRequest;
import com.almaviva.documentale.InternalServerError;
import com.almaviva.documentale.NotFound;
import com.almaviva.documentale.Context;
import com.almaviva.documentale.core.ContentDecoder;
import com.almaviva.documentale.core.ContentEncoder;
import com.almaviva.documentale.core.CreateMetadataAdder;
import com.almaviva.documentale.core.Doc;
import com.almaviva.documentale.core.DocumentRemapper;
import com.almaviva.documentale.core.Finder;
import com.almaviva.documentale.core.Page;
import com.almaviva.documentale.core.SecurityContext;
import com.almaviva.documentale.core.SecurityContextBuilder;
import com.almaviva.documentale.core.UpdateMetadataAdder;
import com.almaviva.documentale.core.WorkingArea;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class Engine {
    @Autowired SecurityContextBuilder scb;
    @Autowired ContentAdapter contentAdapter;
    @Autowired MetaAdapter metaAdapter;
    @Autowired DocumentRemapper[] documentRemappers;
    @Autowired CreateMetadataAdder[] createMetadatas;
    @Autowired UpdateMetadataAdder[] updateMetadatas;
    @Autowired ContentEncoder[] contentEncoders;
    @Autowired ContentDecoder[] contentDecoders;
    @Autowired Finder[] finders;
    
    public Doc get(String id, Context context) {        
        SecurityContext sc = scb.build(context);
        int version = Integer.parseInt(context.get("version", "-1"));
        Doc document = metaAdapter.get(id, version, sc);
        if (document == null) throw new NotFound("document [" + id + "] not found");
        return new WorkingArea(document, null, context, sc)
                    .run(documentRemappers, "DOCUMENT_REMAP").document;
    }

    public Page find(Context context) {
        SecurityContext sc = scb.build(context);

        WorkingArea wa = new WorkingArea(null, null, context, sc)
            .run(finders, "FILTER_SET");

        Doc filter = parseFilter(wa.context.get("filter"));
        LinkedHashMap<String, Integer> sort = toSort(wa.context.get("sort"));
        int limit = Integer.parseInt(wa.context.get("limit"));
        int offset = Integer.parseInt(wa.context.get("offset"));
        Page result = metaAdapter.find(filter, sort, offset, limit, sc);
        result.list = result.list.stream()
                .map(d -> new WorkingArea(d, null, context, sc)
                .run(documentRemappers, "DOCUMENT_REMAP").document)
                .collect(Collectors.toList());
        return result;
    }


    private LinkedHashMap<String, Integer> toSort(String sort) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        Stream.of(sort.split(",")).forEach(e -> result.put(e.substring(1), "+".equals(e.substring(0, 1)) ? 1: -1));
        return result;
    }

    private Doc parseFilter(String filter) {
        if(filter == null) return new Doc();
        try {
            return new ObjectMapper().readValue(filter, new TypeReference<Doc>() {});
        } catch (Exception e) {
            throw new BadRequest("invalid filter");
        }
    }

    public Doc create(Doc document, byte[] bytes, Context context) {
        SecurityContext sc = scb.build(context);
        String md5 = DigestUtils.md5DigestAsHex(bytes);
        document.put("md5", md5);
        WorkingArea wa = new WorkingArea(document, bytes, context, sc)
            .run(createMetadatas, "METADATA_CREATE")
            .run(contentEncoders, "CONTENT_ENCODE");
        wa.document.put("_chunks", contentAdapter.create(md5, wa.bytes, sc));
        return metaAdapter.create(wa.document, sc);
    }

    public Doc content(String id, Context context) {
        SecurityContext sc = scb.build(context);
        Doc document = get(id, context);
        String md5 = (String) document.get("md5");
        byte[] bytes = contentAdapter.bytes(md5, sc);
        WorkingArea wa = new WorkingArea(document, bytes, context, sc)
            .run(contentDecoders, "CONTENT_DECODE");
        if (!md5.equals(DigestUtils.md5DigestAsHex(wa.bytes))) throw new InternalServerError("data corrupted for document [" + id + "]");
        document.put("bytes", wa.bytes);
        return document;
    }

    @SuppressWarnings("unchecked")
	public Doc update(String id, Doc newDocument, byte[] bytes, Context context) {
        SecurityContext sc = scb.build(context);
        Doc document = get(id, context);
        sc.check((List<String>)document.get("write_groups"));
        int version = (int) document.get("version", 0);
        newDocument.remove("create_at", "id", "version");
        document.putAll(newDocument);
        if(bytes != null) document.put("md5", DigestUtils.md5DigestAsHex(bytes));

        WorkingArea wa = new WorkingArea(document, bytes, context, sc)
            .run(updateMetadatas, "METADATA_UPDATE")
            .run(contentEncoders, "CONTENT_ENCODE");
        wa.document.put("version", 1 + version);
        if(bytes != null) wa.document.put("_chunks", contentAdapter.create((String)wa.document.get("md5"), wa.bytes, sc));
        return metaAdapter.create(wa.document, sc);
	}

}