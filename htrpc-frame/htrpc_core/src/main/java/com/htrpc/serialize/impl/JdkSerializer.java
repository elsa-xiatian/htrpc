package com.htrpc.serialize.impl;

import com.htrpc.execptions.SerializeException;
import com.htrpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);)
        {
            outputStream.writeObject(object);
            return baos.toByteArray();
        }catch (IOException e){
            log.error("序列化对象【{}】时发生异常",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T diserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null){
            return null;
        }
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream  = new ObjectInputStream(bais);) {

            return (T)objectInputStream.readObject();
        }catch (IOException | ClassNotFoundException e){
            log.error("反序列化对象【{}】时发生异常",clazz);
            throw new SerializeException(e);
        }
    }
}
