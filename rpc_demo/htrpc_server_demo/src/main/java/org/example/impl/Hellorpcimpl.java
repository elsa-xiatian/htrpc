package org.example.impl;

import com.htrpc.HelloHtrpc;

public class Hellorpcimpl implements HelloHtrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }

}
