package com.ptit.b22cn539.int1433.Service.User;

import com.ptit.b22cn539.int1433.DTO.User.UserLoginRequest;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Repository.IUserRepository;
import com.ptit.b22cn539.int1433.Utils.JwtUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional
    public UserEntity register(UserEntity userEntity) {
        if (this.userRepository.findByUsername(userEntity.getUsername()) != null) {
            throw new RuntimeException();
        }
        userEntity.setPassword(this.passwordEncoder.encode(userEntity.getPassword()));
        return this.userRepository.save(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        return this.userRepository.findAll();
    }
}
