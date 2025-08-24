package com.htrpc.channelHandler.handler;

import com.htrpc.ServiceConfig;
import com.htrpc.enumeration.RequestType;
import com.htrpc.enumeration.RespCode;
import com.htrpc.htrpcBootstrap;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import com.htrpc.transport.message.htrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j

public class MethodCallHandler extends SimpleChannelInboundHandler<htrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, htrpcRequest msg) throws Exception {
        //1.获取负载内容
        RequestPayLoad requestPayLoad = msg.getRequestPayLoad();
        //2.根据负载内容进行方法调用
        Object res = null;
        if(msg.getRequestType() != RequestType.HEART_BEAT.getId()) {
            res = callTargrtMethod(requestPayLoad);
            if (log.isDebugEnabled()) {
                log.debug("请求【{}】已经在服务端完成方法调用", msg.getRequestId());
            }
        }
        //3. 封装响应

        htrpcResponse htrpcresponse = new htrpcResponse();
        htrpcresponse.setCode(RespCode.SUCCESS.getCode());
        htrpcresponse.setRequestId(msg.getRequestId());
        htrpcresponse.setCompressType(msg.getCompressType());
        htrpcresponse.setSerializeType(msg.getSerializeType());
        htrpcresponse.setBody(res);

        //4.写出响应
        ctx.channel().writeAndFlush(htrpcresponse);
    }

    private Object callTargrtMethod(RequestPayLoad requestPayLoad) {
        String interfaceName = requestPayLoad.getInterfaceName();
        String methodName = requestPayLoad.getMethodName();
        Class<?>[] parametersType = requestPayLoad.getParametersType();
        Object[] parametersValue = requestPayLoad.getParametersValue();

        //寻找到暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = htrpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        Object returnValue;

        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
             returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
            log.error("调用请求【{}】的方法【{}】发生异常",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
