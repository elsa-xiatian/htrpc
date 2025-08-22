package com.htrpc.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.htrpc.serialize.Serializer;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if(object == null){
            return null;
        }

        byte[] bytes = JSON.toJSONBytes(object);

       return bytes;
    }

    @Override
    public <T> T diserialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null){
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        return t;
    }
}
