package com.htrpc;

import com.htrpc.discovery.Registry;
import com.htrpc.execptions.NetworkException;
import com.htrpc.proxy.handler.RpcConsumeraInvocationalHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return
     */
    public T get() {
        //使用动态代理完成具体操作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};
        InvocationHandler handler = new RpcConsumeraInvocationalHandler(registry,interfaceRef);
        //使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes,handler);
        return (T)helloProxy;
    }
}
