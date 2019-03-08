package com.almaviva.documentale.interceptors.jwt;

import java.util.Map;

import com.almaviva.documentale.Unauthorized;
import com.almaviva.documentale.core.SecurityContext;
import com.almaviva.documentale.core.SecurityContextBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("jwt")
public class JWTSecurityContextBuilder implements SecurityContextBuilder {

    @Value("${documentale.jwt.secret:secret}") String secret;
    @Value("${documentale.jwt.field:authorization}") String field;

    @Override
    public SecurityContext build(Map<String, String> context) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        String token = context.get(field);
        if (token == null) throw new Unauthorized("jwt token is mandatory");
        DecodedJWT jwt = null;
        try {
            jwt = verifier.verify(token);
        }catch(Exception e)
        {
            throw new Unauthorized("invalid jwt token");
        }
        String user = jwt.getSubject();
        Claim groups = jwt.getClaim("groups");
        if(user == null) throw new Unauthorized("invalid jwt token");
        if(groups.isNull()) throw new Unauthorized("invalid jwt token");
        return new SecurityContext(user, groups.asList(String.class));
    }

}