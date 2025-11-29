package com.marketplace.catalog.web.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {
    private Long id;
    private String login;
    private String role;

    public UserDto(Long id, String login, String role) {
        this.id = id;
        this.login = login;
        this.role = role;
    }
}
