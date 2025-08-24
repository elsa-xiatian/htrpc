package com.htrpc.channelHandler.handler;

import com.htrpc.compress.Compressor;
import com.htrpc.compress.CompressorFactory;
import com.htrpc.htrpcBootstrap;
import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.transport.message.MessageFormatConstant;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class htrpcRequestEncoder extends MessageToByteEncoder<htrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, htrpcRequest msg, ByteBuf out) throws Exception {
        //魔术值
        out.writeBytes(MessageFormatConstant.MAGIC);
        //版本号
        out.writeByte(MessageFormatConstant.VERSION);
        //头部长度
        out.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //总长度暂时不清楚
        out.writerIndex(out.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        //类型
        out.writeByte(msg.getRequestType());
        out.writeByte(msg.getSerializeType());
        out.writeByte(msg.getCompressType());
        //8字节的id
        out.writeLong(msg.getRequestId());
        out.writeLong(msg.getTimeStamp());

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
        byte[] body = null;
        if(msg.getRequestPayLoad() != null) {
            Serializer serializer = SerializerFactory.getSerializer(msg.getSerializeType()).getSerializer();
            body = serializer.serialize(msg.getRequestPayLoad());


            Compressor compressor = CompressorFactory.getCompressor(msg.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }


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
            log.debug("请求【{}】已完成报文的编码",msg.getRequestId());
        }
    }


}
