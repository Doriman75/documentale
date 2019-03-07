package com.almaviva.documentale.interceptors.core;

import java.util.Map;

@FunctionalInterface
public interface ContextBuilder
{
    public Map<String, String> context(Map<String, String> headers, Map<String, String> request);
}