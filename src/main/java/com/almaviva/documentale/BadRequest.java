package com.almaviva.documentale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequest extends RuntimeException {

    private static final long serialVersionUID = -8045973875160313111L;

    public BadRequest(String message)
    {
        super(message);
    }
}