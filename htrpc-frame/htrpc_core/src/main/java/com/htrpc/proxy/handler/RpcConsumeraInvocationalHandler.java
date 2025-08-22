package com.htrpc.proxy.handler;

import com.htrpc.IdGenerator;
import com.htrpc.NettyBootstrapInitilizer;
import com.htrpc.discovery.Registry;
import com.htrpc.enumeration.RequestType;
import com.htrpc.execptions.DiscoveryException;
import com.htrpc.execptions.NetworkException;
import com.htrpc.htrpcBootstrap;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在invoke方法中
 * 1.发现服务，建立连接，发送请求，得到结果
 */
@Slf4j

public class RpcConsumeraInvocationalHandler implements InvocationHandler {

    //此处需要一个注册中心及一个接口
    private Registry registry;

    private Class<?> interfaceRef;

    public RpcConsumeraInvocationalHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("hello proxy");
        //1.发现服务：从注册中心中寻找一个可用的服务
        //传入服务的名字，返回ip+端口
        //todo 每次调用相关方法时都需要去注册中心拉取相关服务列表吗？ 如何合理选择一个可用的服务？而不是只获取第一个？
        InetSocketAddress address = registry.lookup(interfaceRef.getName());
        if(log.isDebugEnabled()){
            log.debug("服务调用方，发现了服务【{}】的可用主机【{}】",interfaceRef.getName()
                    ,address);
        }
        //尝试获取通道
        Channel channel = getAvailableChannel(address);
        if(log.isDebugEnabled()){
            log.debug("获取和【{}】建立的通道",address);
        }

        //封装报文

        RequestPayLoad requestPayLoad = RequestPayLoad.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();


        //todo 需要对各种请求id及类型做处理
        htrpcRequest htrpcrequest = htrpcRequest.builder()
                .requestId(htrpcBootstrap.ID_GENERATOR.getId())
                .compressType(SerializerFactory.getSerializer(htrpcBootstrap.SERIALIZE_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .serializeType((byte) 1)
                .requestPayLoad(requestPayLoad)
                .build();


        //写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        // 需要将completabaFuture暴露出去
        htrpcBootstrap.PENDING_REQUEST.put(1L,completableFuture);

        //将请求写出
        channel.writeAndFlush(htrpcrequest).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });

        //获得结果
        return completableFuture.get(10,TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个可用通道
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        //1.先从缓存中获取
        Channel channel = htrpcBootstrap.CHANNEL_CACHE.get(address);
        //2.拿不到就建立连接
        if(channel == null){
            //await方法会阻塞，会等待连接成功在返回(同步)
//                    channel = NettyBootstrapInitilizer.getBootstrap()
//                            .connect(address).await().channel();

            //异步操作
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitilizer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if(promise.isDone()){
                    if(log.isDebugEnabled()){
                        log.debug("已经和[{}]建立连接",address);
                    }
                    channelFuture.complete(promise.channel());
                }else if(!promise.isSuccess()){
                    channelFuture.completeExceptionally(promise.cause());
                }
            });

            //阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常",e);
                throw new DiscoveryException(e);
            }

            //缓存
            htrpcBootstrap.CHANNEL_CACHE.put(address,channel);
        }
        if(channel == null){
            log.error("获取【{}】的通道时发生异常",address);
            throw new NetworkException("获取通道时发生了异常");
        }
        return channel;
    }
}
