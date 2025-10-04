package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.User;

public interface UserService {
    User registerUser(RegisterUserDto registerUserDto);
}
