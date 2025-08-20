package com.htrpc.transport.message;

public class MessageFormatConstant {
    public final static byte[] MAGIC = "htrpc".getBytes();
    public final static byte VERSION = 1;
    //头部信息的长度
    public final static short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);

    public final static int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int VERSION_LENGTH = 1;
    //头部信息的长度占用字节数
    public static final int HEADER_FIELD_LENGTH = 2;
    public static final int FULL_FIELD_LENGTH = 4;
}
