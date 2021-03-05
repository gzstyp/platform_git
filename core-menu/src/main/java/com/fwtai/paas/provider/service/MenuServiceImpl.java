package com.fwtai.paas.provider.service;

import com.fwtai.paas.api.MenuService;
import com.fwtai.paas.common.tool.ToolClient;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl implements MenuService{

    @Override
    public String add(final String name){
        return ToolClient.createJsonSuccess("添加成功");
    }
}