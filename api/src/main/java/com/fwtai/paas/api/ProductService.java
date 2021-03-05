package com.fwtai.paas.api;

import java.io.Serializable;

public interface ProductService extends Serializable{

    public String list(final String name);
}