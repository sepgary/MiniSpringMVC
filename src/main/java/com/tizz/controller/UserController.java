package com.tizz.controller;

import com.core.annotation.*;
import com.tizz.bean.User;
import com.tizz.service.UserService;

@Controller(value = "userController")
public class UserController {

    @AutoWired(value = "userService")
    private UserService userService;

    @RequestMapping(value = "/findUser")
    public String findUser() {
        return userService.findUser();
    }

    @RequestMapping(value = "/getJson")
    public User getJson(@RequestParam("name") String name) {
        return userService.getUserByName(name);
    }

    @RequestMapping(value = "/welcome")
    public String welcome() {
        return "forward:/welcome";
    }
}
