package com.system.api_gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter implements WebFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        SecurityContextHolder.clearContext();
        // Get jwt from request
        String jwt = getJwtFromRequest(request);

        if (jwt != null) {

//            String userName = jwtService.getUserNameFromJWT(jwt);
//            if (userName != null) {
//
//                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
//
//                if (jwtService.isValidToken(jwt, userDetails)) {
//
//                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
//                            (userDetails, null, userDetails.getAuthorities());
//                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                }
//            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7);
        }

        return null;
    }

    @Override
    public @NotNull Mono<Void> filter(
            @NotNull ServerWebExchange exchange,
            @NotNull WebFilterChain chain) {

        exchange.getResponse()
                .getHeaders().add("web-filter", "web-filter-test");
        return chain.filter(exchange);
    }
}
