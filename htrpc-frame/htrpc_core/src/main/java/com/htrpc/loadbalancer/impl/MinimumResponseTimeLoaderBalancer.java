package com.htrpc.loadbalancer.impl;

import com.htrpc.core.HeartbeatDetector;
import com.htrpc.htrpcBootstrap;
import com.htrpc.loadbalancer.AbstractLoadBalancer;
import com.htrpc.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class MinimumResponseTimeLoaderBalancer  extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {

       return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector{

        private List<InetSocketAddress> serviceList;

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList){
            this.serviceList = serviceList;
        }
        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = htrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if(entry != null){
                if(log.isDebugEnabled()){
                    log.debug("选取了响应时间为【{}】ms的服务节点",entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }


            Channel channel = (Channel)htrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }


    }
}
