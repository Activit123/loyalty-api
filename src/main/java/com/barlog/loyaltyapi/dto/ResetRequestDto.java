package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetRequestDto {
    
    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Format email invalid")
    private String email;

    @NotBlank(message = "Cheia de salvare este obligatorie")
    @Size(min = 8, max = 8, message = "Cheia trebuie să aibă 8 caractere")
    private String recoveryKey;

    @NotBlank(message = "Parola nouă este obligatorie")
    @Size(min = 6, message = "Parola trebuie să aibă minim 6 caractere")
    private String newPassword;
}