package com.almaviva.documentale.engine;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class Unauthorized extends RuntimeException {

    private static final long serialVersionUID = -8045973875160313111L;

    public Unauthorized(String message)
    {
        super(message);
    }
}