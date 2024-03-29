package com.ctillnow.netty;

import com.ctillnow.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

/**
 * Netty服务器
 * @author 流星
 */
@Component
@Slf4j
public class NettyWebSocketServer implements Runnable {

    @Autowired
    private NettyConfig nettyConfig;

    @Autowired
    private WebSocketChannelConfig webSocketChannelConfig;

    /**
     * boss线程组，用于处理连接
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    /**
     * work线程组，用于处理消息
     */
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * 资源关闭——在容器销毁时关闭
     */
    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            //创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(webSocketChannelConfig);
            //启动
            ChannelFuture channelFuture = serverBootstrap.bind(nettyConfig.getPort()).sync();
            log.info("【-----Netty服务端启动成功-----】");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
