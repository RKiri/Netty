package com.weiyuze.nettystudy.s02;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    //Client类保存并初始化Channel 1
    private Channel channel = null;

    public static void main(String[] args) throws InterruptedException {
        Client c = new Client();
        c.connect();
    }

    //改造Client 暴露调用接口
    void connect() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap b = new Bootstrap();

        try {
            ChannelFuture f = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer())
                    .connect("localhost", 8888);

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connected!");
                    } else {
                        System.out.println("connected!");
                        //Client类保存并初始化Channel 2
                        channel = future.channel();
                    }
                }
            });
            f.sync();
            //System.out.println("...");
            f.channel().closeFuture().sync();
            System.out.println("已经退出");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    //封装Client.send(String msg)函数
    void send(String msg){
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        channel.writeAndFlush(buf);
    }

    public void closeConnect() {
        //通知服务器要退出
        send("_bye_");
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new TankMsgEncoder())
                .addLast(new ClientHandler());
    }

}

class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
        //ctx.writeAndFlush(buf);

        ctx.writeAndFlush(new TankMsg(5,8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            String msgAccepted = new String(bytes);//服务器反馈回来的字符串
            //client端接收到channelRead后 拿到单例 更新界面
            ClientFrame.INSTANCE.updateText(msgAccepted);

            //System.out.println(new String(bytes));
        } finally {
            if (buf != null) ReferenceCountUtil.release(buf);
        }

    }
}