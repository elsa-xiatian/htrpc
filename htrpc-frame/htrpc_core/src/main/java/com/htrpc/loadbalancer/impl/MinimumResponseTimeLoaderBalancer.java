package com.htrpc.loadbalancer.impl;

import com.htrpc.htrpcBootstrap;
import com.htrpc.loadbalancer.AbstractLoadBalancer;
import com.htrpc.loadbalancer.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public class MinimumResponseTimeLoaderBalancer  extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return null;
    }

    private static class MinimumResponseTimeSelector implements Selector{

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList){


        }
        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = htrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if(entry != null){
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }


            Channel channel = (Channel)htrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

        @Override
        public void reBalance() {

        }
    }
}
