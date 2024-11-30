package com.example.springrestful.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN,
    EMPLOYEE,
    ;

    public String getName() {
        return name();
    }
}
