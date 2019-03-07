package com.almaviva.documentale.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.almaviva.documentale.interceptors.core.SecurityContext;

public interface MetaAdapter {
    public Map<String, Object> get(String id, int version, SecurityContext sc);
	public List<Map<String, Object>> find(Map<String, Object> filter, LinkedHashMap<String, Integer> sort, int offset, int limit, SecurityContext sc);
    public Map<String, Object> create(Map<String, Object> document, SecurityContext sc);
}