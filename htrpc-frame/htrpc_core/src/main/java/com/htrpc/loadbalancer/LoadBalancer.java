package com.htrpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 */
public interface LoadBalancer  {
    /**
     * 根据服务名获取一个可用的服务
     * @param serviceName
     * @return 服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName);
}
