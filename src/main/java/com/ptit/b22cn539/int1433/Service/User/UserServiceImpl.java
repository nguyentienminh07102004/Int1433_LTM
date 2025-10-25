package com.ptit.b22cn539.int1433.Service.User;

import com.ptit.b22cn539.int1433.DTO.UserLoginRequest;
import com.ptit.b22cn539.int1433.Repository.IUserRepository;
import com.ptit.b22cn539.int1433.Utils.JwtUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements  IUserService {
    IUserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;


    @Override
    @Transactional
    public String login(UserLoginRequest userLoginRequest) {
        var user = this.userRepository.findByUsername(userLoginRequest.getUsername());
        if (user == null || !this.passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException();
        }
        return this.jwtUtils.generateToken(user.getUsername());
    }
}
