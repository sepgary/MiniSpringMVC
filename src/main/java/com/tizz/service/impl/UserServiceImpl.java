package com.tizz.service.impl;

import com.core.annotation.Service;
import com.tizz.bean.User;
import com.tizz.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Override
    public String findUser() {
        System.out.println("success get findUser");
        return "=====> userService findUser";
    }

    @Override
    public User getUserByName(String name) {
        System.out.println("success get getUserByName");
        // 模拟数据库
        List<User> userList = new ArrayList<>();
        userList.add(new User(1, "zs", 1));
        userList.add(new User(2, "ls", 1));
        userList.add(new User(3, "ww", 2));
        userList.add(new User(4, "jack", 1));
        userList.add(new User(5, "tom", 2));

        for (User user : userList) {
            if (user.getName().equals(name)) {
                return user;
            }
        }

        return null;
    }
}
