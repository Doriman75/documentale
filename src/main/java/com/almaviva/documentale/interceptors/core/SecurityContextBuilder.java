package com.almaviva.documentale.interceptors.core;

import java.util.Map;

@FunctionalInterface
public interface SecurityContextBuilder {
    public SecurityContext build(Map<String, String> context);
}