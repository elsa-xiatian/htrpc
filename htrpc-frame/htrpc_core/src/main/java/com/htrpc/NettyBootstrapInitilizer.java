package com.htrpc;

import com.htrpc.channelHandler.ConsumerChannelInitializer;
import com.htrpc.channelHandler.handler.MysimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 给bootstrap提供一个单例
 * todo 这里会有一定问题
 */
@Slf4j
public class NettyBootstrapInitilizer {
    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitilizer() {

    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
