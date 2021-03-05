package com.fwtai.paas.provider.controller;

import com.fwtai.paas.common.tool.ToolClient;
import com.fwtai.paas.provider.service.MenuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MenuController{

    @Autowired
    private MenuServiceImpl menuServiceImpl;

    // http://127.0.0.1:20491/add?name=typ
    @GetMapping("/add")
    public void add(final HttpServletRequest request,final HttpServletResponse response){
        String name = request.getParameter("name");
        final String json = menuServiceImpl.add(name);
        ToolClient.responseJson(json,response);
    }
}