package com.almaviva.documentale.interceptors.ft;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.almaviva.documentale.InternalServerError;
import com.almaviva.documentale.core.CreateMetadataAdder;
import com.almaviva.documentale.core.WorkingArea;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("fulltext")
public class FullTextMetadataAdder implements CreateMetadataAdder {

    @Override
    public WorkingArea perform(WorkingArea wa, String step) {
        Tika tika = new Tika();
        try {
            Metadata metadata = new Metadata();
            String text = tika.parseToString(new ByteArrayInputStream(wa.bytes), metadata);
            wa.document.put("text", text);
            Map<String, String> info = new HashMap<>();
            Stream.of(metadata.names()).forEach(e->info.put(e, metadata.get(e)));
            wa.document.put("info", info);
            return wa;
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage());
        }
    }

}