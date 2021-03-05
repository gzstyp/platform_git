package com.fwtai.paas.provider.controller;

import com.fwtai.paas.common.tool.ToolClient;
import com.fwtai.paas.provider.service.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ProductController{

    @Autowired
    private ProductServiceImpl productServiceImpl;

    // http://127.0.0.1:10291/list?name=tv
    // http://127.0.0.1:20481/product/list?name=tv
    @GetMapping("/list")
    public void list(final HttpServletRequest request,final HttpServletResponse response){
        String name = request.getParameter("name");
        final String json = productServiceImpl.list(name);
        ToolClient.responseJson(json,response);
    }
}