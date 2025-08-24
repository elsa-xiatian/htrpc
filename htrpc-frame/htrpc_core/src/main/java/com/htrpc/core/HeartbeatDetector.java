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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class HeartbeatDetector {
    public static void detectHeartbeat(String ServiceName){
        //1.从注册中心拉去列表并建立连接
        Registry registry = htrpcBootstrap.getInstance().getRegistry();
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
                Channel channel = entry.getValue();

                long start = System.currentTimeMillis();
                //构建一个心跳请求
                htrpcRequest htrpcrequest = htrpcRequest.builder()
                        .requestId(RequestType.HEART_BEAT.getId())
                        .compressType(CompressorFactory.getCompressor(htrpcBootstrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.HEART_BEAT.getId())
                        .serializeType(SerializerFactory.getSerializer(htrpcBootstrap.SERIALIZE_TYPE).getCode())
                        .timeStamp(start)
                        .build();

                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                // 需要将completabaFuture暴露出去
                htrpcBootstrap.PENDING_REQUEST.put(htrpcrequest.getRequestId(),completableFuture);

                channel.writeAndFlush(htrpcrequest).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });

                Long endTime = 0L;
                try {
                    completableFuture.get();
                    endTime = System.currentTimeMillis();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                Long time = endTime - start;
                htrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time,channel);
                log.debug("和服务器的响应时间为" + time);


            }

        }
    }
}
