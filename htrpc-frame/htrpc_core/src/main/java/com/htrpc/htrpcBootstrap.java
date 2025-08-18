package com.htrpc;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.logging.Handler;

@Slf4j
public class htrpcBootstrap {

    //htrpcBootstrap是一个单例：即只有一个实例
    private static htrpcBootstrap htrpcstrap = new htrpcBootstrap();

    private htrpcBootstrap(){
        //构造启动引导程序，在此做初始化
    }
    public static htrpcBootstrap getInstance() {
        return htrpcstrap;
    }

    /**
     * 用来定义应用的名字
     * @param
     * @return this
     */
    public htrpcBootstrap application(String appName) {
        return this;
    }

    /**
     * 用来配置注册中心
     * @return this 当前实例
     */
    public htrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocalConfig 使用的协议
     * @return
     */
    public htrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了: {}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务 将接口及实现注册到服务中心
     * @param service 独立封装的需要发布的服务
     * @return
     */
    public htrpcBootstrap publish(ServiceConfig<?> service) {
        if(log.isDebugEnabled()){
            log.debug("服务{},已经被注册",service.getInterface().getName());
        }
        return this;

    }

    /**
     * 批量发布
     * @param services 需要的服务集合
     * @return
     */

    public htrpcBootstrap publish(List<?> services) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
    }

    /**
     * -----------------------服务调用方的api---------------------------
     * @param reference
     * @return
     */

    public htrpcBootstrap reference(ReferenceConfig<?> reference) {
        //配置reference，方便生成代理对象
        return this;
     }
}
