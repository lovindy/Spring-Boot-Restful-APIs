package com.example.springrestful.controller;


import com.example.springrestful.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/user/")
public class UserController {

    private final UserService userService;

}
