package com.weiyuze.nettystudy.s02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(2);

        try {
            ServerBootstrap b = new ServerBootstrap();
            ChannelFuture f = b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new TankMsgDecoder())
                                    .addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8888)
                    .sync();

            ServerFrame.INSTANCE.updateServerMsg("server started!");
            //System.out.println("Server started!");

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Server s = new Server();
        s.serverStart();

    }


}

class ServerChildHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clients.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            TankMsg tm = (TankMsg) msg;
            System.out.println(tm);
        } finally {
            ReferenceCountUtil.release(msg);
        }


        /*ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        //System.out.println(new String(bytes));
        String s = new String(bytes);

        ServerFrame.INSTANCE.updateClientMsg(s);
        //服务器接收到特定消息进行处理
        //服务器移除channel并关闭ctx
        if (s.equals("_bye_")) {
            ServerFrame.INSTANCE.updateServerMsg("客户端要求退出");
            //System.out.println("客户端要求退出");
            Server.clients.remove(ctx.channel());
            ctx.close();
        } else {
            Server.clients.writeAndFlush(msg);
        }*/

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //删除出现异常的客户端channle，并关闭连接
        Server.clients.remove(ctx.channel());
        ctx.close();
    }
}