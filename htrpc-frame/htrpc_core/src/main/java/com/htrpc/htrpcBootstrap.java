package com.htrpc;

import com.htrpc.annotation.htrpcApi;
import com.htrpc.channelHandler.handler.MethodCallHandler;
import com.htrpc.channelHandler.handler.htrpcRequestDecoder;
import com.htrpc.channelHandler.handler.htrpcResponseEncoder;
import com.htrpc.config.Configuration;
import com.htrpc.core.HeartbeatDetector;
import com.htrpc.discovery.RegistryConfig;
import com.htrpc.loadbalancer.LoadBalancer;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class htrpcBootstrap {


    //htrpcBootstrap是一个单例：即只有一个实例
    private static final htrpcBootstrap htrpcstrap = new htrpcBootstrap();

    private Configuration configuration;

    public static final ThreadLocal<htrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    //连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public final static TreeMap<Long,Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();
    //维护已经发布并暴露的服务列表，key是接口的全限定名，value是服务的配置
    public final static Map<String,ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);
    //定义全局的对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


    private htrpcBootstrap(){
        //构造启动引导程序，在此做初始化
        configuration = new Configuration();
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
        configuration.setAppName(appName);
        return this;
    }

    /**
     * 用来配置注册中心
     * @return this 当前实例
     */
    public htrpcBootstrap registry(RegistryConfig registryConfig) {
        //尝试使用 registryConfig获取一个注册中心
        configuration.setRegistryConfig(registryConfig);

        return this;
    }

    public htrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        //尝试使用 registryConfig获取一个注册中心
       configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocalConfig 使用的协议
     * @return
     */
    public htrpcBootstrap protocal(ProtocalConfig protocalConfig) {
        configuration.setProtocalConfig(protocalConfig);
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
       configuration.getRegistryConfig().getRegistry().register(service);
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

            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();

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
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
     }

    /**
     * 配置序列化的方式
     * @param serializeType
     */
    public htrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if(log.isDebugEnabled()){}
        log.debug("配置了使用的序列化的方式为：[{}]",serializeType);
        return this;
    }

    public htrpcBootstrap compress(String compressType){
        configuration.setCompressType(compressType);
        if(log.isDebugEnabled()){
            log.debug("配置了使用【{}】的压缩方式",compressType);
        }
        return this;
    }

    public htrpcBootstrap scan(String packageName) {
        //通过packageName获得其下所有类的全限定名称
        List<String> classNames =  getAllClassName(packageName);
        //通过反射获取其接口，构建具体实现
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(htrpcApi.class) != null)
                .collect(Collectors.toList());
        for (Class<?> clazz : classes) {
            // 获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException
                     | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            List<ServiceConfig<?>> serviceConfigs = new ArrayList<>();
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if(log.isDebugEnabled()){
                    log.debug("-------->已经通过包扫描，将服务【{}】发布",anInterface);
                }

                //发布
                publish(serviceConfig);
            }

        }

        return this;
    }

    private List<String> getAllClassName(String packageName) {
        //通过packageName获得绝对路径
        String basePath = packageName.replaceAll("\\.","/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(url == null){
         throw new RuntimeException("包扫描时，发现路径不存在");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames,String basepath) {
     //获取文件
        File file = new File(absolutePath);
        //判断文件是否是文件夹
        if(file.isDirectory()){
            //找到文件夹的所有文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()){
                    recursionFile(child.getAbsolutePath(),classNames,basepath);
                } else{
                    String className =  getClassNameByAbsolutePath(child.getAbsolutePath(),basepath);
                   classNames.add(className);
                }
            }
        }else{
            String className =  getClassNameByAbsolutePath(absolutePath,basepath);
            classNames.add(className);
        }

        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath,String basepath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basepath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
