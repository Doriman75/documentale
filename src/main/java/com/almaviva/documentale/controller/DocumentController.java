package com.almaviva.documentale.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almaviva.documentale.engine.BadRequest;
import com.almaviva.documentale.engine.Engine;
import com.almaviva.documentale.interceptors.core.ContextBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    Engine engine;
    @Autowired
    ContextBuilder contextBuilder;

    private Map<String, String> context(Map<String, String> headers, Map<String, String> request) {
        return contextBuilder.context(headers, request);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable String id, @RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> request) {
        return engine.get(id, context(headers, request));
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Map<String, Object> delete(@PathVariable String id, @RequestHeader Map<String, String> headers, @RequestParam Map<String, String> request) {
        Map<String, Object> document = new HashMap<>();
        document.put("deleted", true);
        return engine.update(id, document, null, context(headers, request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Map<String, Object> update(@PathVariable String id, @RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> request, @RequestBody Map<String, Object> document) {
        byte[] bytes = document.containsKey("content") ? Base64.getDecoder().decode((String) document.get("content"))
                : null;
        document.remove("content");
        return engine.update(id, document, bytes, context(headers, request));
    }

    private String contentType(Map<String, Object> document) {
        @SuppressWarnings("unchecked")
        Map<String, String> info = (Map<String, String>) document.get("info");
        if (info == null)
            return "text/plain; charset=ISO-8859-1";
        return info.get("Content-Type");
    }

    @RequestMapping("/{id}/content")
    public ResponseEntity<Object> content(@PathVariable String id, @RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> request) {
        Map<String, Object> document = engine.content(id, context(headers, request));
        String filename = document.get("filename") != null ? document.get("filename").toString() : "file";
        return ResponseEntity
                .ok().contentType(MediaType.parseMediaType(contentType(document)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(document.get("bytes"));
    }

    @RequestMapping("")
    public List<Map<String, Object>> find(@RequestHeader Map<String, String> headers, @RequestParam Map<String, String> request) {
        return engine.find(context(headers, request));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Map<String, Object> create(@RequestBody Map<String, Object> document,
            @RequestHeader Map<String, String> headers, @RequestHeader Map<String, String> request) {
        String content = (String) document.get("content");
        if (content == null)
            throw new BadRequest("content is mandatory");
        byte[] bytes = Base64.getDecoder().decode(content);
        document.remove("content");
        return engine.create(document, bytes, context(headers, request));
    }

    
}