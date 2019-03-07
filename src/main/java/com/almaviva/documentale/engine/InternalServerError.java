package com.almaviva.documentale.engine;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerError extends RuntimeException {

    private static final long serialVersionUID = -8045973875160313111L;

    public InternalServerError(String message)
    {
        super(message);
    }
}