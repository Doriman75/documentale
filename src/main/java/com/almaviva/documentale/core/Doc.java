package com.almaviva.documentale.core;

import java.util.HashMap;
import java.util.Map;

public class Doc extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    public Doc()
    {
        super();
    }
    public Doc(Map<String, Object> m)
    {
        super(m);
    }

    public Doc(String k, Object v)
    {
        super();
        put(k,v);
    }

    public Object get(String k, Object d)
    {
        Object v = get(k);
        return v != null ? v: d;
    }

    public void remove(String...keys)
    {
        for(String k: keys) remove(k);
    }
    
}