package com.htrpc.channelHandler.handler;

import com.htrpc.compress.Compressor;
import com.htrpc.compress.CompressorFactory;
import com.htrpc.enumeration.RequestType;
import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.transport.message.MessageFormatConstant;
import com.htrpc.transport.message.RequestPayLoad;
import com.htrpc.transport.message.htrpcRequest;
import com.htrpc.transport.message.htrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

@Slf4j
public class htrpcResponseDecoder extends LengthFieldBasedFrameDecoder {
    public htrpcResponseDecoder() {
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
        short headdLength = byteBuf.readShort();

        //解析总长度
        int fullLength = byteBuf.readInt();

        //请求类型
        byte responseCode = byteBuf.readByte();

        //序列化类型
        byte serializeType = byteBuf.readByte();

        //压缩类型
        byte compressType = byteBuf.readByte();

        //请求id
        long reqauestId = byteBuf.readLong();

        long timeStamp = byteBuf.readLong();

        htrpcResponse htrpcResponse = new htrpcResponse();
        htrpcResponse.setCode(responseCode);
        htrpcResponse.setCompressType(compressType);
        htrpcResponse.setSerializeType(serializeType);
        htrpcResponse.setRequestId(reqauestId);
        htrpcResponse.setTimeStamp(timeStamp);

        // todo 心跳请求没有负载，此处可判断并直接返回
//        if(requestType == RequestType.HEART_BEAT.getId()){
//            return htrpcrequest;
//        }


        int bodyLength = fullLength - headdLength;

        byte[] payLoad = new byte[bodyLength];
        byteBuf.readBytes(payLoad);

        if( payLoad.length > 0) {

            //得到字节数组后就可以解压缩反序列化
            //解压缩

            Compressor compressor = CompressorFactory.getCompressor(compressType).getCompressor();

            payLoad = compressor.decompress(payLoad);

            //反序列化
            Serializer serializer = SerializerFactory.getSerializer(htrpcResponse.getSerializeType()).getSerializer();
            Object body = serializer.diserialize(payLoad, Object.class);
            htrpcResponse.setBody(body);
        }

        if(log.isDebugEnabled()){
            log.debug("响应【{}】已经在调用端完成解码工作",htrpcResponse.getRequestId());
        }
        return htrpcResponse;
    }
}
