package com.almaviva.documentale.interceptors.aes;

import com.almaviva.documentale.core.ContentDecoder;
import com.almaviva.documentale.core.WorkingArea;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("encrypt")
@Order(9)
public class AESDecoder extends AES implements ContentDecoder {
    public WorkingArea perform(WorkingArea wa, String step) {
        if(wa.bytes == null) return wa;
        Boolean encrypted = (Boolean) wa.document.get("_encrypted");
        if(encrypted == null || !encrypted) return wa;
        wa.bytes = decrypt(wa.bytes);
        return wa;
    }
}