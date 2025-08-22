package com.htrpc.channelHandler.handler;

import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.transport.message.MessageFormatConstant;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import com.htrpc.transport.message.htrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j

public class htrpcResponseEncoder extends MessageToByteEncoder<htrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, htrpcResponse msg, ByteBuf out) throws Exception {
        //魔术值
        out.writeBytes(MessageFormatConstant.MAGIC);
        //版本号
        out.writeByte(MessageFormatConstant.VERSION);
        //头部长度
        out.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //总长度暂时不清楚
        out.writerIndex(out.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        //类型
        out.writeByte(msg.getCode());
        out.writeByte(msg.getSerializeType());
        out.writeByte(msg.getCompressType());
        //8字节的id
        out.writeLong(msg.getRequestId());

//        if(msg.getRequestType() == RequestType.HEART_BEAT.getId()){
//            int writerIndex = out.writerIndex();
//            out.writerIndex(MessageFormatConstant.MAGIC.length
//                    + MessageFormatConstant.VERSION_LENGTH +
//                    MessageFormatConstant.HEADER_FIELD_LENGTH);
//            out.writeInt(MessageFormatConstant.HEADER_LENGTH);
//            out.writerIndex(writerIndex);
//            return;
//        }
        //写入请求体
        Serializer serializer = SerializerFactory.getSerializer(msg.getSerializeType()).getSerializer();
        byte[] body = serializer.serialize(msg.getBody());

        //todo 要做压缩
        if(body != null) {
            out.writeBytes(body);
        }

        int bodyLength = body == null ? 0 : body.length;

        //当前的写指针的位置
        int writerIndex = out.writerIndex();
        //将写指针的位置移动到总长度的位置
        out.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH +
                MessageFormatConstant.HEADER_FIELD_LENGTH);
        out.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //将写指针归位
        out.writerIndex(writerIndex);

        if(log.isDebugEnabled()){
            log.debug("响应【{}】已经在服务端完成编码工作",msg.getRequestId());
        }
    }


}
