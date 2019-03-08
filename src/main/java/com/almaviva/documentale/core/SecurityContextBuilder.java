package com.almaviva.documentale.core;

import java.util.Map;

@FunctionalInterface
public interface SecurityContextBuilder {
    public SecurityContext build(Map<String, String> context);
}
