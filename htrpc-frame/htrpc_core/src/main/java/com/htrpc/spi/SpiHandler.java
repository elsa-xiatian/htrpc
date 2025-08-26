package com.htrpc.spi;

import com.htrpc.config.ObjectWrapper;
import com.htrpc.execptions.SpiException;
import com.htrpc.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class SpiHandler {
    //定义一个basePath
    private static final String BASE_PATH = "META-INF/htrpc-services";

    //定义一个缓存，保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

    private static final Map<Class<?>,List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    //加载当前类之后需要将spi信息进行保存
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if(fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if(children != null) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }

    private static List<String> getImplNames(File child) {
     try(
             FileReader fileReader = new FileReader(child);
             BufferedReader bufferedReader = new BufferedReader(fileReader)
             )
     {
         List<String> implNames = new ArrayList<>();
         while(true){
             String line = bufferedReader.readLine();
             if(line == null || "".equals(line)) break;
             implNames.add(line);
         }
         return implNames;
     } catch (IOException e){
         log.error("读取spi文件时发生异常",e);
     }
     return null;
    }

    public synchronized static <T> ObjectWrapper<T> get(Class<T> clazz){
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return (ObjectWrapper<T>)objectWrappers.get(0);
        }
        //建立缓存
        buildCache(clazz);

        List<ObjectWrapper<?>> res = SPI_IMPLEMENT.get(clazz);
        if(res == null || res.size() == 0){
            return null;
        }

        return (ObjectWrapper<T>) res .get(0);
    }



    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz){
        List<ObjectWrapper<?>> objectWrappers  = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
            return objectWrappers.stream().map(wrapper -> (ObjectWrapper<T>)wrapper)
                    .collect(Collectors.toList());
        }

        buildCache(clazz);
        objectWrappers  = SPI_IMPLEMENT.get(clazz);
        if(objectWrappers != null && objectWrappers.size() > 0){
           return objectWrappers.stream().map(wrapper -> (ObjectWrapper<T>)wrapper)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static void buildCache(Class<?> clazz) {

        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if(implNames == null || implNames.size() == 0){
            return;
        }
        List<ObjectWrapper<?>> impls = new ArrayList<>();

        for (String implName : implNames) {
            try {
                String[] codeAndTypeAndName = implName.split("-");
                if(codeAndTypeAndName.length != 3){
                    throw new SpiException("配置的spi文件不合法");
                }
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];

                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code,type,impl);
                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e){
                log.error("实例化时发生了异常");
            }
        }

        SPI_IMPLEMENT.put(clazz,impls);
    }

    public static void main(String[] args) {

    }
}
