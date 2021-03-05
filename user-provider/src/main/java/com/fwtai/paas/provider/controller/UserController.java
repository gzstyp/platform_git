package com.fwtai.paas.provider.controller;

import com.fwtai.paas.common.tool.ToolClient;
import com.fwtai.paas.provider.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class UserController{

    @Autowired
    private UserServiceImpl userServiceImpl;

    // http://127.0.0.1:20481/login?userName=typ&password=typ
    @GetMapping("/login")
    public void login(final HttpServletRequest request,final HttpServletResponse response){
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        final String json = userServiceImpl.login(userName,password);
        ToolClient.responseJson(json,response);
    }
}