package com.almaviva.documentale.controller;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.almaviva.documentale.BadRequest;
import com.almaviva.documentale.Context;
import com.almaviva.documentale.core.ContextBuilder;
import com.almaviva.documentale.core.Doc;
import com.almaviva.documentale.core.Page;
import com.almaviva.documentale.engine.Engine;

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

    private Context context(Map<String,String> headers, Map<String,String> request) {
        return contextBuilder.context(new Context(headers), new Context(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Doc get(@PathVariable String id, 
                    @RequestHeader Map<String,String> headers, 
                    @RequestParam Map<String,String> request) {
        return engine.get(id, context(headers, request));
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Doc delete(@PathVariable String id, 
                        @RequestHeader Map<String,String> headers, 
                        @RequestParam Map<String,String> request) {
        return engine.update(id, new Doc("deleted", true), null, context(headers, request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Doc update(@PathVariable String id, 
                        @RequestHeader Map<String,String> headers,
                        @RequestParam Map<String,String> request, 
                        @RequestBody Doc document) {
        byte[] bytes = document.containsKey("content") ? 
                        Base64.getDecoder().decode((String) document.get("content"))
                        : null;
        document.remove("content");
        return engine.update(id, document, bytes, context(headers, request));
    }

    @SuppressWarnings("unchecked")
    private String contentType(Doc document) {
        Map<String,String> info = (Map<String,String>) document.get("info");
        if (info == null) return "text/plain; charset=ISO-8859-1";
        return info.get("Content-Type");
    }

    @RequestMapping("/{id}/content")
    public ResponseEntity<Object> content(@PathVariable String id, 
                                            @RequestHeader Map<String,String> headers,
                                            @RequestParam Map<String,String> request) {
        Doc document = engine.content(id, context(headers, request));
        String filename = document.get("filename", "file").toString();
        return ResponseEntity
                .ok().contentType(MediaType.parseMediaType(contentType(document)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(document.get("bytes"));
    }

    @RequestMapping("")
    public ResponseEntity<List<Doc>> find(@RequestHeader Map<String,String> headers, 
                            @RequestParam Map<String,String> request) {

        Page p = engine.find(context(headers, request));

        return ResponseEntity
                .ok()
                .header("offset", "" + p.offset)
                .header("limit", "" + p.limit)
                .header("count", "" + p.count)
                .body(p.list);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Doc create(@RequestBody Doc document,
                        @RequestHeader Map<String,String> headers, 
                        @RequestHeader Map<String,String> request) {
        String content = (String) document.get("content");
        if (content == null)
            throw new BadRequest("content is mandatory");
        byte[] bytes = Base64.getDecoder().decode(content);
        document.remove("content");
        return engine.create(document, bytes, context(headers, request));
    }

    
}