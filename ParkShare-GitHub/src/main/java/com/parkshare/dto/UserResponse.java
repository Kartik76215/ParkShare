package com.parkshare.dto;

import com.parkshare.entity.User;
import com.parkshare.entity.UserRole;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}
