package com.htrpc.channelHandler.handler;

import com.htrpc.htrpcBootstrap;
import com.htrpc.transport.message.htrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 测试类
 */
@Slf4j
public class MysimpleChannelInboundHandler extends SimpleChannelInboundHandler<htrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, htrpcResponse msg) throws Exception {
        Object returnValue = msg.getBody();
        CompletableFuture<Object> completableFuture = htrpcBootstrap.PENDING_REQUEST.get(1l);
        completableFuture.complete(returnValue);

        if(log.isDebugEnabled()){
            log.debug("已经寻找到编号为【{}】的completableFuture，处理结果",msg.getRequestId());
        }
    }
}
