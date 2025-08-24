package com.htrpc.discovery.impl;

import com.htrpc.Constant;
import com.htrpc.ServiceConfig;
import com.htrpc.discovery.AbstractRegistry;
import com.htrpc.utils.zookeeper.Netutils;
import com.htrpc.utils.zookeeper.ZookeeperNode;
import com.htrpc.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosRegistry extends AbstractRegistry {
    //维护一个实例
    private ZooKeeper zooKeeper;

    public NacosRegistry(){
        this.zooKeeper = ZookeeperUtil.createZK();
    }

    public NacosRegistry(String connectString, int timeout){
        this.zooKeeper = ZookeeperUtil.createZK(connectString,timeout);
    }
    @Override
    public void register(ServiceConfig<?> service) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();
        //持久节点(该服务的父节点)
        if(!ZookeeperUtil.exists(zooKeeper,parentNode,null)){
            ZookeeperNode node = new ZookeeperNode(parentNode,null);
            ZookeeperUtil.createNode(zooKeeper,node,null, CreateMode.PERSISTENT);
        }

        //创建本机临时节点,ip:port
        //服务提供方的端口一般自己设定，还需要一个获取ip的方法
        //ip需要一个局域网ip，不是本机ip

        //TODO 端口后续要处理
        String node = parentNode + "/" + Netutils.getIp() + ":" + 8088;
        if(!ZookeeperUtil.exists(zooKeeper,node,null)){
            ZookeeperNode znode = new ZookeeperNode(node,null);
            ZookeeperUtil.createNode(zooKeeper,znode,null, CreateMode.EPHEMERAL);
        }

        if(log.isDebugEnabled()){
            log.debug("服务{},已经被注册",service.getInterface().getName());
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String name) {
        return null;
    }
}
