package com.htrpc;

import com.htrpc.impl.Hellorpcimpl;

public class Application {
    public static void main(String[] args) {
        //注册服务，启动服务
        //1.封装要发布的服务
        ServiceConfig<HelloHtrpc> service = new ServiceConfig<>();
        service.setInterface(HelloHtrpc.class);
        service.setRef(new Hellorpcimpl());

        //2.通过启动引导程序，启动服务提供方
        //  （1）配置-- 应用的名称 --注册中心 (序列化协议，压缩方式)
        htrpcBootstrap.getInstance()
                .application("first-htrpc-application")
                //配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                //协议
                .protocal(new ProtocalConfig("jdk"))
                //发布服务
                .publish(service)
                //启动服务
                .start();
    }
}
