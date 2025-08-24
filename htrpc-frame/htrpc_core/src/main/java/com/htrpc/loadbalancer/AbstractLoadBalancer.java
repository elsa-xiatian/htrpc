package com.htrpc.loadbalancer;

import com.htrpc.discovery.Registry;
import com.htrpc.htrpcBootstrap;
import com.htrpc.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{


    //一个服务会匹配一个selector
    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

        Selector selector = cache.get(serviceName);

        if(selector == null){
            List<InetSocketAddress> serviceList = htrpcBootstrap.getInstance().getRegistry().lookup(serviceName);
            //提供算法选取合适节点
            selector = getSelector(serviceList);
            cache.put(serviceName,selector);
        }



        return selector.getNext();
    }

    /**
     * 由子类进行扩展
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);


}
