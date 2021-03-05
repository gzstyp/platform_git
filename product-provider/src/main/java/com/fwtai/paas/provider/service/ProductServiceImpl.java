package com.fwtai.paas.provider.service;

import com.fwtai.paas.api.ProductService;
import com.fwtai.paas.common.tool.ToolClient;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService{

    @Override
    public String list(final String name){
        return ToolClient.createJsonSuccess("获取'"+name+"'成功");
    }
}