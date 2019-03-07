package com.almaviva.documentale.interceptors.core;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class DefaultCreateMetadataAdder implements CreateMetadataAdder {

    @Override
    public WorkingArea perform(WorkingArea wa, String step) {        
        wa.document.put("id", UUID.randomUUID().toString());
        wa.document.put("created_at", new Date().toInstant());
        if(wa.bytes != null) wa.document.put("size", wa.bytes.length);
        wa.document.put("version", 0);
        return wa;
    }

}