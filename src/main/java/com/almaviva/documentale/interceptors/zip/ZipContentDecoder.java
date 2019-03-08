package com.almaviva.documentale.interceptors.zip;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

import com.almaviva.documentale.InternalServerError;
import com.almaviva.documentale.core.ContentDecoder;
import com.almaviva.documentale.core.WorkingArea;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("zip")
@Order(10)
public class ZipContentDecoder implements ContentDecoder
{
    public WorkingArea perform(WorkingArea wa, String step){
        if(wa.bytes == null) return wa;
        Boolean zipped = (Boolean) wa.document.get("_zipped");
        if(zipped == null || !zipped) return wa;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(wa.bytes);
            wa.bytes = IOUtils.toByteArray(new GZIPInputStream(in));
            return wa;
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage());
        }
    }
}