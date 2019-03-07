package com.almaviva.documentale.interceptors.core;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class DefaultUpdateMetadataAdder implements UpdateMetadataAdder {

    @Override
    public WorkingArea perform(WorkingArea wa, String step) {        
        wa.document.put("updated_at", new Date().toInstant());
        if(wa.bytes != null) wa.document.put("size", wa.bytes.length);
        return wa;
    }

}