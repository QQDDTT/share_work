package com.nick.share_work.frame.authentication.model;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 用户权限
 * 
 * @author nick
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Authority implements GrantedAuthority {
    private static final long serialVersionUID = 1L;
    public static final Authority ADMIN = new Authority("AUTHORITY_ADMIN");
    public static final Authority USER = new Authority("AUTHORITY_USER");
    private String authority;

    public Authority() {}

    public Authority(String authority) {
        this.authority = authority;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Authority other = (Authority) obj;
        if (authority == null) {
            if (other.authority != null)
                return false;
        } else if (!authority.equals(other.authority))
            return false;
        return true;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        return result;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }
    
}


