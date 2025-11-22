package com.barlog.loyaltyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManualXpRequestDto {

    @NotBlank(message = "Emailul este obligatoriu.")
    @Email(message = "Adresa de email trebuie să fie validă.")
    private String email;

    @NotNull(message = "Suma de XP este obligatorie.")
    @Min(value = 1, message = "Suma de XP trebuie să fie cel puțin 1.")
    private Integer amount;

}