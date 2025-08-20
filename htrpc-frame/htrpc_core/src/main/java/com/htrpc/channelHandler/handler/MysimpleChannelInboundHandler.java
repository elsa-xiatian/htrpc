package com.htrpc.channelHandler.handler;

import com.htrpc.htrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 测试类
 */
public class MysimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String res = msg.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = htrpcBootstrap.PENDING_REQUEST.get(1l);
        completableFuture.complete(res);
    }
}
