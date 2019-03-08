package com.almaviva.documentale.core;

@FunctionalInterface
public interface Interceptor
{
    public WorkingArea perform(WorkingArea wa, String step);
}