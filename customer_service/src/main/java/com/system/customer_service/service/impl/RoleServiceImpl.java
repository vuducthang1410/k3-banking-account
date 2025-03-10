package com.system.customer_service.service.impl;

import com.system.common_library.enums.Role;
import com.system.customer_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    @Value("${app.keycloak.realm}")
    @NonFinal
    String realm;

    Keycloak keycloak;

    @Override
    public void assignRole(String userId, Role roleName) {

        UserResource user = getUser(userId);
        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName.toString()).toRepresentation();
        user.roles().realmLevel().add(Collections.singletonList(representation));
    }

    @Override
    public void deleteRoleFromUser(String userId, Role roleName) {

        UserResource user = getUser(userId);
        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName.toString()).toRepresentation();
        user.roles().realmLevel().remove(Collections.singletonList(representation));
    }

    public UserResource getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    private UsersResource getUsersResource(){
        return keycloak.realm(realm).users();
    }

    private RolesResource getRolesResource(){
        return keycloak.realm(realm).roles();
    }
}
