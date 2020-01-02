package org.example.danmu;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class DanmuNettyServer {

    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024) //配置
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //处理器
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-decodec",new HttpRequestDecoder());
                            pipeline.addLast("http-aggregator",new HttpObjectAggregator(65536));
                            pipeline.addLast("http-encodec",new HttpResponseEncoder());
                            pipeline.addLast("http-chunked",new ChunkedWriteHandler());
                            pipeline.addLast("http-request",new HttpRequestHandler("/ws"));
                            pipeline.addLast("WebSocket-protocol",new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast("WebSocket-request",new TextWebSocketFrameHandler());
                        }
                    });
            System.out.println("弹幕服务器启动");
            ChannelFuture channelFuture = bootstrap.bind(9000).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
