package com.htrpc.core;

import com.htrpc.NettyBootstrapInitilizer;
import com.htrpc.compress.CompressorFactory;
import com.htrpc.discovery.Registry;
import com.htrpc.enumeration.RequestType;
import com.htrpc.htrpcBootstrap;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetector {
    public static void detectHeartbeat(String ServiceName){
        //1.从注册中心拉去列表并建立连接
        Registry registry = htrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(ServiceName);
        //2.对连接进行缓存
        for (InetSocketAddress address : addresses) {
            try {
                if(!htrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitilizer.getBootstrap().connect(address).sync().channel();
                    htrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        //3.定期发送消息
        Thread thread = new Thread(() ->
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000)
        ,"HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private static class MyTimerTask extends TimerTask{

        @Override
        public void run() {

            htrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            //遍历所有channel
            Map<InetSocketAddress, Channel> cache = htrpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                int tryTimes = 3;
                while(tryTimes > 0) {
                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();
                    //构建一个心跳请求
                    htrpcRequest htrpcrequest = htrpcRequest.builder()
                            .requestId(RequestType.HEART_BEAT.getId())
                            .compressType(CompressorFactory.getCompressor(htrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEART_BEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(htrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();

                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    // 需要将completabaFuture暴露出去
                    htrpcBootstrap.PENDING_REQUEST.put(htrpcrequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(htrpcrequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });

                    Long endTime = 0L;
                    try {
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        //发生问题优先考虑重试
                        tryTimes--;

                        log.error("与地址为【{}】的主机连接发生异常", channel.remoteAddress());
                        //重试次数用尽
                        if(tryTimes == 0){
                            //将失效地址移除服务列表
                            htrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        try {
                            Thread.sleep(10 * (new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                        continue;
                    }

                    Long time = endTime - start;
                    htrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("和服务器的响应时间为" + time);
                    break;
                }

            }

        }
    }
}
