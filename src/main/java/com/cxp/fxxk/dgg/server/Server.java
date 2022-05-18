package com.cxp.fxxk.dgg.server;

import com.cxp.fxxk.dgg.inbound.Socks5InboundHandler;
import com.cxp.fxxk.dgg.inbound.Socks5InitialRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import sun.misc.Signal;

import java.io.IOException;

/**
 * @author chenxinpei
 * @description
 * @createDate 2022/5/17 9:43 PM
 */
@Component
@Slf4j
public class Server implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("spring项目启动成功，初始化代理服务器");
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        ServerBootstrap server = new ServerBootstrap();
        try {
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 3)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            pipeline.addLast(Socks5ServerEncoder.DEFAULT);

                            // 处理初始化请求
                            pipeline.addLast(new Socks5InitialRequestDecoder());
                            pipeline.addLast(new Socks5InitialRequestHandler());

                            //处理connection请求
                            pipeline.addLast(new Socks5CommandRequestDecoder());
                            pipeline.addLast(new Socks5InboundHandler(clientGroup));
                        }
                    });
            ChannelFuture future = server.bind(8001).sync();
            log.info("socks5 netty server has started on port 8001");

            startInjectProcess();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void startInjectProcess() throws IOException {
        // 获取当前系统信息 如果为windows 则执行windows相关操作
        String osName = System.getProperty("os.name");
        Process process = null;
        if (osName.contains("Windows")) {
            // windows系统 执行windows相关逻辑
            process = Runtime.getRuntime().exec("cmd /k start ./windows/node.exe ./script/src/main.js");
            log.info("脚本注入进程启动成功");
            Process finalProcess = process;
            Signal.handle(new Signal("TERM"), sig -> {
                log.info("尝试关闭注入进程");
                finalProcess.destroy();
            });
        }
    }
}
