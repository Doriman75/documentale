package com.almaviva.documentale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class Forbidden extends RuntimeException {

    private static final long serialVersionUID = -8045973875160313111L;

    public Forbidden(String message)
    {
        super(message);
    }
}