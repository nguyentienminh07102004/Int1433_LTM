package com.ptit.b22cn539.int1433.Controller.SocketIO;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ptit.b22cn539.int1433.Models.UserEntity;
import com.ptit.b22cn539.int1433.Service.User.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSocketIOController {
    IUserService userService;

    @OnEvent(value = "topic/getAllUsers")
    public void getAllUsers(SocketIOClient client) {
        List<UserEntity> users = this.userService.getAllUsers();
        client.sendEvent("topic/getAllUsersResponse", users);
    }
}
