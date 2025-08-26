package com.htrpc.impl;

import com.htrpc.HelloHtrpc;
import com.htrpc.HelloHtrpc2;
import com.htrpc.annotation.htrpcApi;

@htrpcApi
public class Hellorpcimpl2 implements HelloHtrpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }

}
