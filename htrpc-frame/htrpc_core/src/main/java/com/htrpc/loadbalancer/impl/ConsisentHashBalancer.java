package com.htrpc.loadbalancer.impl;

import com.htrpc.execptions.LoadBalancerExecption;
import com.htrpc.htrpcBootstrap;
import com.htrpc.loadbalancer.AbstractLoadBalancer;
import com.htrpc.loadbalancer.Selector;
import com.htrpc.transport.message.htrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsisentHashBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {

        return new ConsisentHashSelector(serviceList,128);
    }

    /**
     * 一致性hash算法的具体实现
     */
    private static class  ConsisentHashSelector implements Selector{

        //hash环用来存储服务器节点
        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();

        private int virtualNodes;


        public ConsisentHashSelector(List<InetSocketAddress> serviceList,int virtualNodes) {
            //将节点转为虚拟节点进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                //把每个节点传入到hash环中
                addNodeToCircle(inetSocketAddress);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            htrpcRequest htrpcRequest = htrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(htrpcRequest.getRequestId());
            int hash = hash(requestId);
            // 判断该hash值是否能直接落在一个服务器上
            if(!circle.containsKey(hash)){
                //寻找最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }

        /**
         * 将每个节点挂载到hash环上
         * @param inetSocketAddress 节点地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            //为每个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                circle.put(hash,inetSocketAddress);
                if(log.isDebugEnabled()){
                    log.debug("hash为【{}】的节点已经挂载到环上",hash);
                }
            }
        }

        private int hash(String s){
            MessageDigest md;
            try{
                md = MessageDigest.getInstance("MD5");
            }catch (NoSuchAlgorithmException e){
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8;
                if(digest[i] < 0){
                    res = res | (digest[i] & 255);
                }else{
                    res = res | digest[i];
                }
            }
            return res;
        }

        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            //为每个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                circle.remove(hash);
            }
        }


    }
}
