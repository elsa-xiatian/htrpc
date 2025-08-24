package com.htrpc;

import com.htrpc.channelHandler.handler.MethodCallHandler;
import com.htrpc.channelHandler.handler.htrpcRequestDecoder;
import com.htrpc.channelHandler.handler.htrpcResponseEncoder;
import com.htrpc.core.HeartbeatDetector;
import com.htrpc.discovery.Registry;
import com.htrpc.discovery.RegistryConfig;
import com.htrpc.loadbalancer.LoadBalancer;
import com.htrpc.loadbalancer.impl.ConsisentHashBalancer;
import com.htrpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class htrpcBootstrap {


    public static final int PORT = 8090;
    //htrpcBootstrap是一个单例：即只有一个实例
    private static final htrpcBootstrap htrpcstrap = new htrpcBootstrap();

    //定义相关基础配置
    private String appName = "default"; //应用名称

    private RegistryConfig registryConfig;
    private ProtocalConfig protocalConfig;


    public static final IdGenerator ID_GENERATOR = new IdGenerator(1,2);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";
    //TODO 待处理

    public static final ThreadLocal<htrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private Registry registry;
    public static LoadBalancer LOAD_BALANCER;
    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public final static TreeMap<Long,Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //维护已经发布并暴露的服务列表，key是接口的全限定名，value是服务的配置
    public final static Map<String,ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    //定义全局的对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    //维护一个zookeeper实例
    // private ZooKeeper zooKeeper;


    private htrpcBootstrap(){
        //构造启动引导程序，在此做初始化
    }
    public static htrpcBootstrap getInstance() {
        return htrpcstrap;
    }

    /**
     * 用来定义应用的名字
     * @param
     * @return this
     */
    public htrpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置注册中心
     * @return this 当前实例
     */
    public htrpcBootstrap registry(RegistryConfig registryConfig) {
        //尝试使用 registryConfig获取一个注册中心
        this.registry = registryConfig.getRegistry();
        htrpcBootstrap.LOAD_BALANCER = new ConsisentHashBalancer();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocalConfig 使用的协议
     * @return
     */
    public htrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        this.protocalConfig = protocalConfig;
        if(log.isDebugEnabled()){
            log.debug("当前工程使用了: {}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }

    /**
     * 发布服务 将接口及实现注册到服务中心
     * @param service 独立封装的需要发布的服务
     * @return
     */
    public htrpcBootstrap publish(ServiceConfig<?> service) {
        //抽象了注册中心的概念，使用注册中心的一个实现完成注册
        registry.register(service);
        SERVERS_LIST.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量发布
     * @param services 需要的服务集合
     * @return
     */

    public htrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {

            //2.需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //核心部分，需要添加很多入站和出战的handler
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new htrpcRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new htrpcResponseEncoder())
                                    ;
                        }
                    });

            //4.绑定端口

            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();

            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){

        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * -----------------------服务调用方的api---------------------------
     * @param reference
     * @return
     */

    public htrpcBootstrap reference(ReferenceConfig<?> reference) {

        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        //配置reference，方便生成代理对象
        reference.setRegistry(registry);
        return this;
     }

    /**
     * 配置序列化的方式
     * @param serializeType
     */
    public htrpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if(log.isDebugEnabled()){}
        log.debug("配置了使用的序列化的方式为：[{}]",serializeType);
        return this;
    }

    public htrpcBootstrap compress(String compressType){
        COMPRESS_TYPE = compressType;
        if(log.isDebugEnabled()){
            log.debug("配置了使用【{}】的压缩方式",compressType);
        }
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }
}
