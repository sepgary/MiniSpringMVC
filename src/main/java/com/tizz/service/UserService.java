package com.tizz.service;

import com.tizz.bean.User;

public interface UserService {

    String findUser();

    User getUserByName(String name);
}
