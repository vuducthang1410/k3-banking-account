package com.system.transaction_service.service;

import com.system.transaction_service.dto.user.UserDetailCustom;
import com.system.transaction_service.mapper.UserDetailMapper;
import com.system.transaction_service.util.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MessageSource messageSource;

    private final UserDetailMapper userDetailMapper;

//    @DubboReference
//    private final CustomerDubboService customerDubboService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

//        Optional<UserDetailCustom> result = Optional.ofNullable(
//                userDetailMapper.detailToDetailCustom(customerDubboService.loadUserByUsername(username)));
        Optional<UserDetailCustom> result =
                Optional.of(UserDetailCustom.builder()
                        .username("admin")
                        .password("password")
                        .id("Unknown")
                        .role("CUSTOMER")
                        .build());

        log.info("User detail custom information: {}", result.get());
        return result
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage(Constant.INVALID_ACCOUNT, null, LocaleContextHolder.getLocale())));
    }

}
