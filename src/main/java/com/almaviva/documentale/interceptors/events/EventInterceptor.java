package com.almaviva.documentale.interceptors.events;

import com.almaviva.documentale.interceptors.core.ContentDecoder;
import com.almaviva.documentale.interceptors.core.ContentEncoder;
import com.almaviva.documentale.interceptors.core.DocumentRemapper;
import com.almaviva.documentale.interceptors.core.Finder;
import com.almaviva.documentale.interceptors.core.CreateMetadataAdder;
import com.almaviva.documentale.interceptors.core.WorkingArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EventInterceptor implements 
    ContentDecoder, 
    ContentEncoder,
    CreateMetadataAdder,
    DocumentRemapper,
    Finder
{
    private static final Logger logger = LoggerFactory.getLogger(EventInterceptor.class);

    @Override
    public WorkingArea perform(WorkingArea wa, String step) {
        logger.debug("executing " + step);
        return wa;
    }
    
}    