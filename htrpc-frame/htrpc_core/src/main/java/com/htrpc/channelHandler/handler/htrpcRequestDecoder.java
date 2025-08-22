package com.htrpc.channelHandler.handler;

import com.htrpc.enumeration.RequestType;
import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.serialize.SerializerWrapper;
import com.htrpc.transport.message.MessageFormatConstant;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
public class htrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public htrpcRequestDecoder() {
        super(
                //最大帧长度
                MessageFormatConstant.MAX_FRAME_LENGTH
                //长度字段的偏移量
                ,MessageFormatConstant.MAGIC.length +
                        MessageFormatConstant.VERSION_LENGTH +
                        MessageFormatConstant.HEADER_FIELD_LENGTH
                //长度的字段的长度
                , MessageFormatConstant.FULL_FIELD_LENGTH
                , -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        +MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH)
                ,0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);

        if(decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //1.解析魔术值
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测是否匹配
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("获得的请求不合法");
            }
        }
        //解析版本号
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的版本不支持");
        }

        //解析头部长度
        short headerLength = byteBuf.readShort();

        //解析总长度
        int fullLength = byteBuf.readInt();

        //请求类型
        byte requestType = byteBuf.readByte();

        //序列化类型
        byte serializeType = byteBuf.readByte();

        //压缩类型
        byte compressType = byteBuf.readByte();

        //请求id
        long reqauestId = byteBuf.readLong();

        htrpcRequest htrpcrequest = new htrpcRequest();
        htrpcrequest.setRequestType(requestType);
        htrpcrequest.setCompressType(compressType);
        htrpcrequest.setSerializeType(serializeType);
        htrpcrequest.setRequestId(reqauestId);

        // 心跳请求没有负载，此处可判断并直接返回
        if(requestType == RequestType.HEART_BEAT.getId()){
            return htrpcrequest;
        }


        int payLoadLength = fullLength - headerLength;

        byte[] payLoad = new byte[payLoadLength];
        byteBuf.readBytes(payLoad);

        //得到字节数组后就可以解压缩反序列化
        //todo 解压缩

        //反序列化
        Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
        RequestPayLoad requestPayLoad = serializer.diserialize(payLoad, RequestPayLoad.class);
        htrpcrequest.setRequestPayLoad(requestPayLoad);

        if(log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成解码",htrpcrequest.getRequestId());
        }
        return htrpcrequest;
    }
}
