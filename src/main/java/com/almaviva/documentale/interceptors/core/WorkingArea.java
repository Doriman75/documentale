package com.almaviva.documentale.interceptors.core;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkingArea{
    private static final Logger logger = LoggerFactory.getLogger(WorkingArea.class);
 
    public Map<String, Object> document;
    public byte[] bytes;
    public Map<String, String> context;
    public SecurityContext sc;
    public WorkingArea(Map<String, Object> document, byte[] bytes, Map<String, String> context, SecurityContext sc){
        this.document = document;
        this.bytes = bytes;
        this.context = context;
        this.sc = sc;
    }
    public WorkingArea(WorkingArea result){
        this(result.document, result.bytes, result.context, result.sc);
    }

    public WorkingArea run(Interceptor[] interceptors,String step)
    {
        WorkingArea result = new WorkingArea(this);
        if(interceptors == null || interceptors.length == 0) return result;

        long start = System.currentTimeMillis();
        for(Interceptor i: interceptors) {
            long s = System.currentTimeMillis();
            try{
                result = i.perform(result, step);                
            }
            finally{
                logger.debug("  " + i.getClass().getSimpleName() + " done in " + (System.currentTimeMillis() - s) + " ms");
            }
        }
        logger.debug("Interceptor Chain done in " + (System.currentTimeMillis() - start) + " ms");
        return result;
    }
}