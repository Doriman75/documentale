package com.almaviva.documentale.core;

import com.almaviva.documentale.Context;

@FunctionalInterface
public interface ContextBuilder
{
    public Context context(Context headers, Context request);
}