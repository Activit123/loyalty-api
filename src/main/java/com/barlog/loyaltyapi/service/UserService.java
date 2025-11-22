package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User registerUser(RegisterUserDto registerUserDto);
    User claimReceiptCoins(User currentUser, ClaimRequestDto claimRequest);
    User updateNickname(User currentUser, String newNickname);
    User updateAvatar(User currentUser, MultipartFile file);
}
