package com.htrpc.config;

import com.htrpc.compress.Compressor;
import com.htrpc.compress.CompressorFactory;
import com.htrpc.loadbalancer.LoadBalancer;
import com.htrpc.serialize.Serializer;
import com.htrpc.serialize.SerializerFactory;
import com.htrpc.spi.SpiHandler;

import java.util.List;

public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        if(loadBalancerWrappers != null && loadBalancerWrappers.size() > 0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> objectWrappers = SpiHandler.getList(Compressor.class);
        if(objectWrappers != null){
            objectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrapper = SpiHandler.getList(Serializer.class);
        if(serializerObjectWrapper != null){
            serializerObjectWrapper.forEach(SerializerFactory::addSerializer);
        }
    }
}
