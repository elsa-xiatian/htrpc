package com.htrpc.discovery;

import com.htrpc.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心，统一对服务进行注册（注册，下线，发现）
 *
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig 服务的配置内容
     */
     void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    InetSocketAddress lookup(String serviceName);
}
