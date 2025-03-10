package com.system.customer_service.controller;

import com.system.common_library.enums.Role;
import com.system.customer_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PutMapping("/assign/users/{userId}")
    public ResponseEntity<?> assignRole(@PathVariable String userId, @RequestParam Role roleName) {

        roleService.assignRole(userId, roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @DeleteMapping("/remove/users/{userId}")
    public ResponseEntity<?> unAssignRole(@PathVariable String userId, @RequestParam Role roleName) {

        roleService.deleteRoleFromUser(userId, roleName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
