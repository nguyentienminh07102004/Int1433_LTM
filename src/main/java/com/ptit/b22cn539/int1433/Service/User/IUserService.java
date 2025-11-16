package com.ptit.b22cn539.int1433.Service.User;

import com.ptit.b22cn539.int1433.DTO.User.UserLoginRequest;
import com.ptit.b22cn539.int1433.DTO.User.UserResponse;
import com.ptit.b22cn539.int1433.Models.UserEntity;

import java.util.List;

public interface IUserService {
    String login(UserLoginRequest userLoginRequest);
    UserEntity register(UserEntity userEntity);
    List<UserResponse> getAllUsers(String username);
}
