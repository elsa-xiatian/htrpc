package com.htrpc.serialize;

import com.htrpc.compress.CompressWrapper;
import com.htrpc.compress.Compressor;
import com.htrpc.config.ObjectWrapper;
import com.htrpc.serialize.impl.JdkSerializer;
import com.htrpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static{
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);

        SERIALIZER_CACHE_CODE.put((byte) 1,jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2,json);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType 序列化类型
     * @return 包装类
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType){
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if(serializerWrapper == null){
            log.error("未找到您定义的序列化方式");
            return SERIALIZER_CACHE_CODE.get("jdk");
        }
        return serializerWrapper;
    }

    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode){
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if(serializerWrapper == null){
            log.error("未找到您定义的序列化方式");
            return SERIALIZER_CACHE_CODE.get("jdk");
        }
        return serializerWrapper;
    }

    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper){
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(),serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(),serializerObjectWrapper);
    }
}
