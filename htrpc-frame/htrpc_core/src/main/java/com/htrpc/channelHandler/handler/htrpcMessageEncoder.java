package com.htrpc.channelHandler.handler;

import com.htrpc.enumeration.RequestType;
import com.htrpc.execptions.DiscoveryException;
import com.htrpc.transport.message.MessageFormatConstant;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.server.quorum.QuorumCnxManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
@Slf4j

public class htrpcMessageEncoder extends MessageToByteEncoder<htrpcRequest> {
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
        byte[] body = getBodyBytes(msg.getRequestPayLoad());
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
    }

    private byte[] getBodyBytes(RequestPayLoad requestPayLoad) {

        if(requestPayLoad == null){
            return null ;
        }
        //todo 针对不同的消息类型要做不同的处理
        //把对象变成字节数据->序列化
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayLoad);
            return baos.toByteArray();
        } catch (IOException e){
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
