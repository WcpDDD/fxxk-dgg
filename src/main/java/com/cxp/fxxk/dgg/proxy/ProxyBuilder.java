package com.cxp.fxxk.dgg.proxy;

import io.netty.handler.proxy.HttpProxyHandler;

import java.net.InetSocketAddress;

public class ProxyBuilder {

    public static HttpProxyHandler createHttpProxyHandler() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1",
                8003);

        HttpProxyHandler handler = new HttpProxyHandler(inetSocketAddress);

        return handler;
    }
}
