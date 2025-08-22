package com.htrpc.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializeUtil {

    public static byte[] serialize(Object object){
        if(object == null){
            return null ;
        }
        //todo 针对不同的消息类型要做不同的处理
        //把对象变成字节数据->序列化
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e){
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
