package com.almaviva.documentale.interceptors.aes;

import com.almaviva.documentale.core.ContentEncoder;
import com.almaviva.documentale.core.WorkingArea;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("encrypt")
@Order(-9)
public class AESEncoder extends AES implements ContentEncoder {
    public WorkingArea perform(WorkingArea wa, String step) {
        if(wa.bytes == null) return wa;
        wa.bytes = encrypt(wa.bytes);
        wa.document.put("_encrypted", true);
        return wa;
    }
}