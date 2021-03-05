package com.fwtai.paas.provider.service;

import com.fwtai.paas.api.UserService;
import com.fwtai.paas.common.tool.ToolClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private ConfigurableApplicationContext context;

    @Value("${server.port}")
    private String port;

    @Override
    public String login(final String userName,final String password){
        if(userName.equals(password)){
            return ToolClient.createJsonSuccess("登录成功");
        }
        final String result = context.getEnvironment().getProperty("server.port")+",userName->"+userName+",password->"+password+",port->"+port;
        return ToolClient.queryJson(result);
    }
}