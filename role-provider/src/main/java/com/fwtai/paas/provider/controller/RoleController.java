package com.fwtai.paas.provider.controller;

import com.fwtai.paas.common.tool.ToolClient;
import com.fwtai.paas.provider.service.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class RoleController{

    @Autowired
    private RoleServiceImpl roleServiceImpl;

    // http://127.0.0.1:10281/list
    @GetMapping("/list")
    public void list(final HttpServletRequest request,final HttpServletResponse response){
        final String json = roleServiceImpl.list();
        ToolClient.responseJson(json,response);
    }
}