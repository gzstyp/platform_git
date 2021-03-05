package com.fwtai.paas.provider.service;

import com.fwtai.paas.api.RoleService;
import com.fwtai.paas.common.tool.ToolClient;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService{

    @Override
    public String list(){
        return ToolClient.createJsonSuccess("操作成功");
    }
}