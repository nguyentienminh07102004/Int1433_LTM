package com.ptit.b22cn539.int1433.DTO.User;

import com.ptit.b22cn539.int1433.Configuration.UserStatus;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String fullName;
    String username;
    UserStatus status;

    public UserResponse(UserEntity userEntity, UserStatus status) {
        this.id = userEntity.getId();
        this.fullName = userEntity.getFullName();
        this.username = userEntity.getUsername();
        this.status = status;
    }
}
