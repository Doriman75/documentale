package com.almaviva.documentale.interceptors.zip;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import com.almaviva.documentale.engine.InternalServerError;
import com.almaviva.documentale.interceptors.core.ContentEncoder;
import com.almaviva.documentale.interceptors.core.WorkingArea;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("zip")
@Order(-10)
public class ZipContentEncoder implements ContentEncoder {
    public WorkingArea perform(WorkingArea wa, String step) {
        if(wa.bytes == null) return wa;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(wa.bytes);
            gzip.flush();
            gzip.close();
            wa.bytes = baos.toByteArray();
            wa.document.put("_zipped_size", wa.bytes.length);
            wa.document.put("_zipped", true);
            return wa;
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage());
        }
    }
}