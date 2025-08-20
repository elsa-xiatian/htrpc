package com.htrpc.channelHandler;

import com.htrpc.channelHandler.handler.MysimpleChannelInboundHandler;
import com.htrpc.channelHandler.handler.htrpcMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                //netty自带日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器
                .addLast(new htrpcMessageEncoder())

                .addLast(new MysimpleChannelInboundHandler());
    }
}
