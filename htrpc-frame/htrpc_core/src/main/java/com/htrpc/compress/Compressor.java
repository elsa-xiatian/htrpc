package com.htrpc.compress;

public interface Compressor {
    //对字节数据进行压缩
    byte[] compress(byte[] bytes );

    //解压缩
    byte[] decompress(byte[] bytes);
}
