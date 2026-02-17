package com.barlog.loyaltyapi.dto;

import lombok.Data;

@Data
public class AllUsersDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
