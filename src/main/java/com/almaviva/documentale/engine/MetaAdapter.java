package com.almaviva.documentale.engine;

import java.util.LinkedHashMap;
import java.util.List;

import com.almaviva.documentale.core.Doc;
import com.almaviva.documentale.core.SecurityContext;

public interface MetaAdapter {
    public Doc get(String id, int version, SecurityContext sc);
	public List<Doc> find(Doc filter, LinkedHashMap<String, Integer> sort, int offset, int limit, SecurityContext sc);
    public Doc create(Doc document, SecurityContext sc);
}