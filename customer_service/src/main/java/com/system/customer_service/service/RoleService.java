package com.system.customer_service.service;

import com.system.common_library.enums.Role;

public interface RoleService {

    void assignRole(String userId , Role roleName);
    void deleteRoleFromUser(String userId ,Role roleName);
}
