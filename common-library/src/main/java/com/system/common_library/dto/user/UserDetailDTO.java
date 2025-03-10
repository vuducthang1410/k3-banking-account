package com.system.common_library.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO implements Serializable {

    private String id;
    private String username;
    private String password;
    private String role;
}
