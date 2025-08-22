package com.htrpc.serialize;

/**
 * 序列化器
 */
public interface Serializer {
    /**
     * 抽象的用来做序列化的方法
     * @param object 待序列化的对象实例
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 反序列化的方法
     * @param bytes 待反序列化的字节数组
     * @param clazz  目标类的class对象
     * @return 目标实例
     * @param <T> 目标类泛型
     */

    <T> T diserialize(byte[] bytes,Class<T> clazz);
}
