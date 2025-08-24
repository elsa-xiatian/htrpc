package com.htrpc.compress;

import com.htrpc.compress.impl.GzipCompressor;
import com.htrpc.serialize.SerializerWrapper;
import com.htrpc.serialize.impl.JdkSerializer;
import com.htrpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, CompressWrapper> COMPRESS_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte,CompressWrapper> COMPRESS_CACHE_CODE = new ConcurrentHashMap<>(8);

    static{
        CompressWrapper gzip = new CompressWrapper((byte) 1,"gzip",new GzipCompressor());
        COMPRESS_CACHE.put("gzip",gzip);

        COMPRESS_CACHE_CODE.put((byte) 1,gzip);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param compressType 压缩类型
     * @return 包装类
     */
    public static CompressWrapper getCompressor(String compressType){
        CompressWrapper compressWrapper = COMPRESS_CACHE.get(compressType);
        if(compressWrapper == null){
            log.error("传入的压缩方式有误");
            return COMPRESS_CACHE.get("gzip");
        }
        return compressWrapper;
    }

    public static CompressWrapper getCompressor(byte compressCode){
        CompressWrapper compressWrapper = COMPRESS_CACHE_CODE.get(compressCode);

        if(compressWrapper == null){
            log.error("传入的压缩方式有误");
            return COMPRESS_CACHE.get("gzip");
        }
        return compressWrapper;

    }
}
