package com.htrpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述请求调用方请求的接口方法的描述
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayLoad implements Serializable {
    //1.接口的名字
    private String interfaceName;
    //2.调用的方法名
    private String methodName;
    //3.参数列表(参数类型 -> 指定方法和具体参数 -> 指定哪个重载方法)
    private Class<?>[] parametersType;
    private Object[] parametersValue;
    //4.返回值的封装
    private Class<?> returnType;


}
