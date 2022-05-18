package com.cxp.fxxk.dgg.proxy;

import io.netty.resolver.InetSocketAddressResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DingdaxueHosts {
    private static Map<String, String> ipMap = new ConcurrentHashMap<>();
    private final static String HOST_NAME = "saas.daxue.dingtalk.com";

    public static boolean isIpProxy(String ip) {
        return ipMap.containsKey(ip);
    }

    public static String getHostName() {
        return HOST_NAME;
    }

    public static boolean isDomainProxy(String domain) {
        return domain.equals(HOST_NAME);
    }

    @PostConstruct
    public void postConstruct() {
        new Thread(() -> {
            while (true) {
                InetAddress[] inetAddresses = new InetAddress[0];
                try {
                    inetAddresses = InetAddress.getAllByName(HOST_NAME);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                for (InetAddress inetAddress : inetAddresses) {
                    ipMap.put(inetAddress.getHostAddress(), inetAddress.getHostName());
                }
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
