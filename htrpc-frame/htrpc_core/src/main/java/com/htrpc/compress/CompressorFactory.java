package com.htrpc.compress;

import com.htrpc.compress.impl.GzipCompressor;
import com.htrpc.config.ObjectWrapper;
import com.htrpc.serialize.SerializerWrapper;
import com.htrpc.serialize.impl.JdkSerializer;
import com.htrpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESS_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,ObjectWrapper<Compressor>> COMPRESS_CACHE_CODE = new ConcurrentHashMap<>(8);

    static{
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1,"gzip",new GzipCompressor());
        COMPRESS_CACHE.put("gzip",gzip);
        COMPRESS_CACHE_CODE.put((byte) 1,gzip);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressType 压缩类型
     * @return 包装类
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressType){
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESS_CACHE.get(compressType);
        if(compressorObjectWrapper == null){
            log.error("传入的压缩方式有误");
            return COMPRESS_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    public static ObjectWrapper<Compressor> getCompressor(byte compressCode){
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESS_CACHE_CODE.get(compressCode);

        if(compressorObjectWrapper == null){
            log.error("传入的压缩方式有误");
            return COMPRESS_CACHE.get("gzip");
        }
        return compressorObjectWrapper;

    }

    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper){
        COMPRESS_CACHE.put(compressorObjectWrapper.getName(),compressorObjectWrapper);
        COMPRESS_CACHE_CODE.put(compressorObjectWrapper.getCode(),compressorObjectWrapper);
    }
}
