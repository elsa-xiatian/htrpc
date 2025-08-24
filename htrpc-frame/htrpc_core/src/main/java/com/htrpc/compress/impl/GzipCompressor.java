package com.htrpc.compress.impl;

import com.htrpc.compress.Compressor;
import com.htrpc.execptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {

        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);)
        {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] res = bos.toByteArray();
            if(log.isDebugEnabled()){
                log.debug("对字节数组进行了压缩，长度由【{}】压缩至【{}】",bytes.length,res.length);
            }
            return res;
        }catch (IOException e){
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }

    }

    @Override
    public byte[] decompress(byte[] bytes) {

        try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bis);)
        {
            byte[] res = gzipInputStream.readAllBytes();
            if(log.isDebugEnabled()){
                log.debug("对字节数组进行了解压缩，长度由【{}】变为【{}】",bytes.length,res.length);
            }
            return res;
        }catch (IOException e){
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }

    }
}
