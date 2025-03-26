package com.system.common_library.dto.user;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class CustomUserDetail extends User implements UserDetails, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String cifCode;

    public CustomUserDetail(String username, String password, Collection<? extends GrantedAuthority> authorities,String cifCode) {
        super(username, password, authorities);
        this.cifCode=cifCode;
    }
}
