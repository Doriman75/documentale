package com.almaviva.documentale.core;

import java.util.List;

import com.almaviva.documentale.Forbidden;

public class SecurityContext {

    public String user;
    public List<String> groups;

    public SecurityContext(String user, List<String> groups)
    {
        this.user = user;
        this.groups = groups;
    }

    public void check(List<String> documentGroups)
    {
        if(documentGroups == null) return;
        if(groups.stream().filter(g -> documentGroups.contains(g)).count() == 0)
            throw new Forbidden("user not authorized");
    }

}