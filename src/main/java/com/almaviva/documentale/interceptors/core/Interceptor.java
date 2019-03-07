package com.almaviva.documentale.interceptors.core;

@FunctionalInterface
public interface Interceptor
{
    public WorkingArea perform(WorkingArea wa, String step);
}