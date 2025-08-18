package com.htrpc;

import com.htrpc.execptions.ZookeeperExecption;
import com.htrpc.utils.zookeeper.ZookeeperNode;
import com.htrpc.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

/**
 * 注册中心管理页面
 */
@Slf4j
public class application {
    public static void main(String[] args) {

            //创建一个zookeeper实例
            ZooKeeper zookeeper = ZookeeperUtil.createZK();
            String basePath = "/htrpc-metadata";
            String providerPath = basePath + "/providers";
            String consumerPath = basePath + "/consumers";

            ZookeeperNode baseNode = new ZookeeperNode("/htrpc-metadata",null);
            ZookeeperNode providersNode = new ZookeeperNode( providerPath,null);
            ZookeeperNode consumersNode = new ZookeeperNode(consumerPath,null);

            //创建节点
            List.of(baseNode,providersNode,consumersNode).forEach(node ->{
                ZookeeperUtil.createNode(zookeeper,node,null,CreateMode.PERSISTENT);
            });

            //关闭连接
            ZookeeperUtil.close(zookeeper);
        }
    }
