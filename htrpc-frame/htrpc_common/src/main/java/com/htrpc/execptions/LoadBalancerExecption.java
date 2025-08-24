package com.htrpc.execptions;

public class LoadBalancerExecption extends RuntimeException{
    public LoadBalancerExecption() {
    }

    public LoadBalancerExecption(Throwable cause) {
        super(cause);
    }
}
