package com.almaviva.documentale.core;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.almaviva.documentale.BadRequest;

import org.springframework.stereotype.Component;

@Component
public class DefaultFinder implements Finder {

    @Override
    public WorkingArea perform(WorkingArea wa, String step) {
        wa.context.put("limit", toLimit(wa.context.get("limit")));
        wa.context.put("offset", "" + toOffset(wa.context.get("offset")));
        wa.context.put("sort", toSort(wa.context.get("sort")));
        return wa;
    }

    private String toSort(String sort) {
        return sort == null ? "+created_at": Stream.of(sort.split(",")).map(e -> e.trim()).collect(Collectors.joining(","));
    }

    private String toLimit(String limit) {
        if(limit == null) return "10";
        try{
            return "" + Integer.parseInt(limit);
        }catch(Exception e)
        {
            throw new BadRequest("invalid limit");
        }
    }

    private String toOffset(String offset) {
        if(offset == null) return "0";
        try{
            return "" + Integer.parseInt(offset);
        }catch(Exception e)
        {
            throw new BadRequest("invalid offset");
        }
    }

}