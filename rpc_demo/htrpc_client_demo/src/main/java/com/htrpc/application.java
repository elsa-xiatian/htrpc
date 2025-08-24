package com.htrpc;

import com.htrpc.core.HeartbeatDetector;
import com.htrpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class application {
    public static void main(String[] args) {
        //使用ReferenceConfig进行封装
        //reference中存在生成代理的模板方法

        ReferenceConfig<HelloHtrpc> reference = new ReferenceConfig<>(); //服务引用配置类，用来描述调用哪个接口的服务
        reference.setInterface(HelloHtrpc.class);

        //代理作用：
        // 1.连接注册中心
        // 2.获取服务列表
        // 3.选择一个服务并建立连接
        // 4.发送请求，并携带所需信息（接口名，参数列表，方法名）
        // 5.获得结果
        htrpcBootstrap.getInstance()
                .application("first-htrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .reference(reference)
                .compress("gzip");

        //获取代理对象
        HelloHtrpc htrpc = reference.get();
        for (int i = 0; i < 10; i++) {
            String sayHi = htrpc.sayHi("你好");
            log.info("sayhi -->{}",sayHi);
        }

    }
}
