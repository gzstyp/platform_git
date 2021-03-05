package com.fwtai.paas.api;

import java.io.Serializable;

public interface UserService extends Serializable{

    public String login(final String userName,final String password);
}