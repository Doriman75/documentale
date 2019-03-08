package com.almaviva.documentale;

import java.util.LinkedHashMap;
import java.util.Map;

public class Context extends LinkedHashMap<String, String>
{

    private static final long serialVersionUID = 1L;

    public Context()
    {
        super();
    }

    public Context(Map<String, String> m)
    {
        super(m);
    }

    public String get(String k, String d)
    {
        String v = get(k);
        return v != null ? v : d;
    }

}