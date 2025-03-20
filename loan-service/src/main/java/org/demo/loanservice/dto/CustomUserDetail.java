package org.demo.loanservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
@Getter
@Setter
public class CustomUserDetail extends User implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;
    private String cifCode;

    public CustomUserDetail(String username, String password, Collection<? extends GrantedAuthority> authorities,String cifCode) {
        super(username, password, authorities);
        this.cifCode=cifCode;
    }
}
