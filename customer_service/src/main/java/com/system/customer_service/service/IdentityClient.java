package com.system.customer_service.service;

import com.system.customer_service.dto.identity.*;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "identity-client", url = "${app.keycloak.serverUrl}")
public interface IdentityClient {
    @PostMapping(
            value = "/realms/klb/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse exchangeToken(@QueryMap TokenExchangeParam param);

    @PostMapping(
            value = "/realms/klb/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse authenticate(@QueryMap AuthenticateParam param);

    @PostMapping(
            value = "/realms/klb/protocol/openid-connect/token/introspect",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> introspect(@QueryMap IntrospectParam param);

    @PostMapping(
            value = "/realms/klb/protocol/openid-connect/logout",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> logout(@QueryMap LogoutParam param);

    @PostMapping(value = "/admin/realms/klb/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(@RequestHeader("authorization") String token, @RequestBody UserCreationParam param);
}
