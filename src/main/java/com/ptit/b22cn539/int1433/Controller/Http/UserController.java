package com.ptit.b22cn539.int1433.Controller.Http;

import com.ptit.b22cn539.int1433.DTO.User.UserLoginRequest;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Service.User.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping(value = "/login")
    public String login(@RequestBody UserLoginRequest userLoginRequest) {
        return this.userService.login(userLoginRequest);
    }

    @PostMapping(value = "/register")
    public UserEntity register(@RequestBody UserEntity userEntity) {
        return this.userService.register(userEntity);
    }
}
