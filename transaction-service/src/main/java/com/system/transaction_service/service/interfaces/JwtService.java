package com.system.transaction_service.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String getUserNameFromJWT(String token);

    boolean isValidToken(String token, UserDetails userDetails);
}
