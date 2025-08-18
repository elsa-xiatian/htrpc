package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class client {

    public void run(){
        //1.定义线程池
        NioEventLoopGroup group = new NioEventLoopGroup();

        //2.启动一个客户端所需的辅助类
        Bootstrap bootstrap = new Bootstrap();
        try {
        bootstrap.group(group)
                .remoteAddress(new InetSocketAddress(8080))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MyChannelHandler2());
                    }
                });

        //尝试连接服务器
        ChannelFuture channelFuture = null;

            channelFuture = bootstrap.connect().sync();

            //获取channel并写出数据
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));
            //阻塞程序等待接受消息
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new client().run();
    }
}
