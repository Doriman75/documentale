package com.almaviva.documentale.engine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.almaviva.documentale.interceptors.core.ContentDecoder;
import com.almaviva.documentale.interceptors.core.ContentEncoder;
import com.almaviva.documentale.interceptors.core.Finder;
import com.almaviva.documentale.interceptors.core.SecurityContext;
import com.almaviva.documentale.interceptors.core.SecurityContextBuilder;
import com.almaviva.documentale.interceptors.core.UpdateMetadataAdder;
import com.almaviva.documentale.interceptors.core.CreateMetadataAdder;
import com.almaviva.documentale.interceptors.core.WorkingArea;
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
    @Autowired com.almaviva.documentale.interceptors.core.DocumentRemapper[] documentRemappers;
    @Autowired CreateMetadataAdder[] createMetadatas;
    @Autowired UpdateMetadataAdder[] updateMetadatas;
    @Autowired ContentEncoder[] contentEncoders;
    @Autowired ContentDecoder[] contentDecoders;
    @Autowired Finder[] finders;
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String id, Map<String, String> context) {        
        SecurityContext sc = scb.build(context);
        int version = context.containsKey("version") ? Integer.parseInt(context.get("version")): -1;
        Map<String, Object> document = metaAdapter.get(id, version, sc);
        if (document == null) throw new NotFound("document [" + id + "] not found");
        sc.check((List<String>)document.get("read_groups"));

        return new WorkingArea(document, null, context, sc)
                    .run(documentRemappers, "DOCUMENT_REMAP").document;
    }

    public List<Map<String, Object>> find(Map<String, String> context) {
        SecurityContext sc = scb.build(context);

        WorkingArea wa = new WorkingArea(null, null, context, sc)
            .run(finders, "FILTER_SET");

        Map<String, Object> filter = parseFilter(wa.context.get("filter"));
        LinkedHashMap<String, Integer> sort = toSort(wa.context.get("sort"));
        int limit = Integer.parseInt(wa.context.get("limit"));
        int offset = Integer.parseInt(wa.context.get("offset"));

        return metaAdapter
                .find(filter, sort, offset, limit, sc).stream()
                .map(d -> new WorkingArea(d, null, context, sc).run(documentRemappers, "DOCUMENT_REMAP").document)
                .collect(Collectors.toList());
    }


    private LinkedHashMap<String, Integer> toSort(String sort) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        Stream.of(sort.split(",")).forEach(e -> result.put(e.substring(1), "+".equals(e.substring(0, 1)) ? 1: -1));
        return result;
    }

    private Map<String, Object> parseFilter(String filter) {
        if(filter == null) return new HashMap<String, Object>();
        try {
            return new ObjectMapper().readValue(filter, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BadRequest("invalid filter");
        }
    }

    public Map<String, Object> create(Map<String, Object> document, byte[] bytes, Map<String, String> context) {
        SecurityContext sc = scb.build(context);
        String md5 = DigestUtils.md5DigestAsHex(bytes);
        document.put("md5", md5);
        WorkingArea wa = new WorkingArea(document, bytes, context, sc)
            .run(createMetadatas, "METADATA_CREATE")
            .run(contentEncoders, "CONTENT_ENCODE");
        wa.document.put("_chunks", contentAdapter.create(md5, wa.bytes, sc));
        return metaAdapter.create(wa.document, sc);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> content(String id, Map<String, String> context) {
        SecurityContext sc = scb.build(context);
        Map<String, Object> document = get(id, context);
        sc.check((List<String>)document.get("write_groups"));
        String md5 = (String) document.get("md5");
        byte[] bytes = contentAdapter.bytes(md5, sc);
        com.almaviva.documentale.interceptors.core.WorkingArea wa = new WorkingArea(document, bytes, context, sc)
            .run(contentDecoders, "CONTENT_DECODE");
        if (!md5.equals(DigestUtils.md5DigestAsHex(wa.bytes))) throw new InternalServerError("data corrupted for document [" + id + "]");
        document.put("bytes", wa.bytes);
        return document;
    }

    @SuppressWarnings("unchecked")
	public Map<String, Object> update(String id, Map<String, Object> newDocument, byte[] bytes, Map<String, String> context) {
        SecurityContext sc = scb.build(context);
        Map<String, Object> document = get(id, context);
        sc.check((List<String>)document.get("write_groups"));
        int version = document.containsKey("version") ? (int) document.get("version"): 0;
        newDocument.remove("create_at");
        newDocument.remove("id");
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