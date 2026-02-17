package com.barlog.loyaltyapi.service;

import com.barlog.loyaltyapi.dto.AllUsersDTO;
import com.barlog.loyaltyapi.dto.ClaimRequestDto;
import com.barlog.loyaltyapi.dto.PointDistributionDto;
import com.barlog.loyaltyapi.dto.RegisterUserDto;
import com.barlog.loyaltyapi.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User registerUser(RegisterUserDto registerUserDto);
    User claimReceiptCoins(User currentUser, ClaimRequestDto claimRequest);
    User updateNickname(User currentUser, String newNickname);
    User updateAvatar(User currentUser, MultipartFile file);
    User generateNewRecoveryKey(User currentUser);
    List<AllUsersDTO> getAllUsers();

    void distributePoints(User user, PointDistributionDto request);
}
