package com.htrpc;

public class ServiceConfig<T> {
    private Class<?> interfaceProvider;

    private Object ref;

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }
}
