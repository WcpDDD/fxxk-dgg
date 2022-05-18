package com.cxp.fxxk.dgg.inbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenxinpei
 * @description
 * @createDate 2022/5/17 9:52 PM
 */
@Slf4j
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
        boolean failure = msg.decoderResult().isFailure();

        if (failure) {
            log.error("初始化socks5失败，请检查是否是socks5协议");
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead(msg);
            return;
        }

        // 不需要认证
        Socks5InitialResponse socks5InitialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
        ctx.writeAndFlush(socks5InitialResponse);

        ctx.pipeline().remove(this);
        ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
    }
}
