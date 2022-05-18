package com.cxp.fxxk.dgg.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenxinpei
 * @description
 * @createDate 2022/5/17 10:03 PM
 */
@Slf4j
public class Dest2ClientInboundHandler extends ChannelInboundHandlerAdapter {
    private final ChannelHandlerContext clientChannelHandlerContext;

    public Dest2ClientInboundHandler(ChannelHandlerContext clientChannelHandlerContext) {
        this.clientChannelHandlerContext = clientChannelHandlerContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("target地址:" + ctx.channel().remoteAddress());
        clientChannelHandlerContext.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.trace("代理服务器和目标服务器的连接已经断开，即将断开客户端和代理服务器的连接");
        clientChannelHandlerContext.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Dest2ClientInboundHandler exception", cause);
    }
}
