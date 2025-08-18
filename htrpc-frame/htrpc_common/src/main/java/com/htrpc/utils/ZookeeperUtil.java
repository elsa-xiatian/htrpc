package com.htrpc.utils;

import com.htrpc.Constant;
import com.htrpc.execptions.ZookeeperExecption;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
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
}
