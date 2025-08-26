package com.htrpc.watcher;

import com.htrpc.NettyBootstrapInitilizer;
import com.htrpc.discovery.Registry;
import com.htrpc.htrpcBootstrap;
import com.htrpc.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if(event.getType() == Event.EventType.NodeChildrenChanged){
            if(log.isDebugEnabled()){
                log.debug("检测到有节点上/下线，重新拉取列表");
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = htrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName);
            for (InetSocketAddress address : addresses) {
                if(!htrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitilizer.getBootstrap()
                                .connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    htrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }
            for (Map.Entry<InetSocketAddress, Channel> entry : htrpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if(!addresses.contains(entry.getKey())){
                    htrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            //获得负载均衡器，进行重新的loadBalancer
            LoadBalancer loadBalancer = htrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];

    }
}
