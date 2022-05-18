package com.cxp.fxxk.dgg.inbound;

import com.cxp.fxxk.dgg.proxy.DingdaxueHosts;
import com.cxp.fxxk.dgg.proxy.ProxyBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.resolver.InetSocketAddressResolver;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenxinpei
 * @description
 * @createDate 2022/5/17 9:58 PM
 */
@Slf4j
@AllArgsConstructor
public class Socks5InboundHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {
    private EventLoopGroup eventExecutors;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        Socks5AddressType socks5AddressType = msg.dstAddrType();
        if (!msg.type().equals(Socks5CommandType.CONNECT)) {
            log.debug("receive commandRequest type={}", msg.type());
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead(msg);
            return;
        }

        log.debug("准备连接目标服务器，ip={},port={}", msg.dstAddr(), msg.dstPort());

        // 如果地址类型为ipv4 则查询是否为云课堂ip
        // 如果地址类型为域名 则查询是否为云课堂域名
        // 其余的均不走代理
        boolean isProxy = false;

        if (socks5AddressType.equals(Socks5AddressType.IPv4)) {
            if (DingdaxueHosts.isIpProxy(msg.dstAddr())) {
                isProxy = true;
            }
        } else if (socks5AddressType.equals(Socks5AddressType.DOMAIN)) {
            if (DingdaxueHosts.isDomainProxy(msg.dstAddr())) {
                isProxy = true;
            }
        }

        Bootstrap bootstrap = new Bootstrap();
        boolean finalIsProxy = isProxy;
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加服务端写客户端的Handler

                        if (finalIsProxy) {
                            log.info("检测到云课堂流量，添加代理");
                            ch.pipeline().addLast(ProxyBuilder.createHttpProxyHandler());
                        }
                        ch.pipeline().addLast(new Dest2ClientInboundHandler(ctx));
                    }
                });

        ChannelFuture future = isProxy ? bootstrap.connect(DingdaxueHosts.getHostName(), msg.dstPort())
                : bootstrap.connect(msg.dstAddr(), msg.dstPort());
        future.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                log.debug("目标服务器连接成功");
                //添加客户端转发请求到服务端的Handler
                ctx.pipeline().addLast(new Client2DestInboundHandler(future1));
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socks5AddressType);
                ctx.writeAndFlush(commandResponse);
                ctx.pipeline().remove(Socks5InboundHandler.class);
                ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
            } else {
                log.error("连接目标服务器失败,address={},port={}", msg.dstAddr(), msg.dstPort());
                DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, socks5AddressType);
                ctx.writeAndFlush(commandResponse);
                future1.channel().close();
            }
        });
    }

}
