package com.htrpc.utils.zookeeper;

import com.htrpc.Constant;
import com.htrpc.execptions.ZookeeperExecption;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtil {

    /**
     * 使用默认配置创建的zookeeper实例
     * @return zookee实例
     */

    public static ZooKeeper createZK(){
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;
        return createZK(connectString,timeout);
    }

    public static ZooKeeper createZK(String connectString,int timeout ){

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
           final ZooKeeper zookeeper = new ZooKeeper(connectString,timeout,event -> {
                if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("客户端已连接成功");
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();

            return zookeeper;

        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常",e);
            throw new ZookeeperExecption();
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zookeeper 实例
     * @param node 节点
     * @param watcher watcher
     * @param createMode 类型
     * @return true/false 创建成功与否,异常抛出
     */
    public static boolean createNode(ZooKeeper zookeeper,ZookeeperNode node,Watcher watcher,CreateMode createMode){
        try {
            if(zookeeper.exists(node.getNodePath(),watcher) == null){
                String res = zookeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】,成功创建",res);
                return true;
            }else{
                if(log.isDebugEnabled()){
                    log.info("节点【{}】已经存在",node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时产生如下异常：",e);
            throw new ZookeeperExecption();
        }
    }

    /**
     * 关闭zookeeper
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时出现异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断节点是否存在
     * @param zk zk实例
     * @param node 节点路径
     * @param watcher
     * @return ture,false
     */
    public static boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            return zk.exists(node,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在时发生异常",e,node);
            throw new ZookeeperExecption(e);
        }
    }

    /**
     * 查询一个节点的子元素
     * @param zooKeeper zk实例
     * @param serviceNode 服务节点
     * @return 子元素列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode,Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException |InterruptedException e) {
            log.error("获取节点【{}】的子元素时发生异常",serviceNode,e);
            throw new RuntimeException(e);
        }
    }
}
