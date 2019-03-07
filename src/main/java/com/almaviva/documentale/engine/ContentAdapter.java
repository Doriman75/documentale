package com.almaviva.documentale.engine;

import com.almaviva.documentale.interceptors.core.SecurityContext;

public interface ContentAdapter {
    public int create(String md5, byte[] bytes, SecurityContext sc);
    public byte[] bytes(String md5, SecurityContext sc);

}