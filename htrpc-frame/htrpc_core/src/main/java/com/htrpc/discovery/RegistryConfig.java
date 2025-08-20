package com.htrpc.discovery;

import com.htrpc.Constant;
import com.htrpc.discovery.Registry;
import com.htrpc.discovery.impl.NacosRegistry;
import com.htrpc.discovery.impl.ZookeeperRegistry;
import com.htrpc.execptions.DiscoveryException;

public class RegistryConfig {

    //定义连接的url zookeeper://127.0.0.1:2181
    private final String connectString;
    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 使用简单工厂完成
     * @return
     */
    public Registry getRegistry() {
        //1.需要获取注册中心的类型
        String registryType = getRegistryType(connectString,true).toLowerCase().trim();
        if(registryType.equals("zookeeper")){
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if(registryType.equals("nacos")){
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host,Constant.TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心:");
    }

    private String getRegistryType(String connectString,boolean ifType){
        String[] typeAndHost =  connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if(ifType){
            return typeAndHost[0];
        }else{
            return typeAndHost[1];
        }
    }
}
