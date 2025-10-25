package com.ptit.b22cn539.int1433.Service.User;

import com.ptit.b22cn539.int1433.DTO.UserLoginRequest;
import com.ptit.b22cn539.int1433.Models.UserEntity;

public interface IUserService {
    String login(UserLoginRequest userLoginRequest);
    UserEntity register(UserEntity userEntity);
}
