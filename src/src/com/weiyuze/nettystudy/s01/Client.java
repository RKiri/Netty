package com.weiyuze.nettystudy.s01;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {//Netty基于事件模型 只需处理事件发生后做什么处理

    public static void main(String[] args) throws InterruptedException {//多线程 封装在EventLoopGroup
        //线程池 处理channel上所有事件(connect、读)
        //Event：网络IO事件，能否连接、接收(accept)、读、写
        //Loop：循环不停处理
        //Group：组，组成一个池子
        //extends EventExecutorGroup 事件执行器组成的组 extends ScheduledExecutorService 预先设置执行线程的服务(Java concurrent包 并发)
        EventLoopGroup group = new NioEventLoopGroup(1);//默认CPU核数*2
        Bootstrap b = new Bootstrap();//辅助启动类 启Socket连接远程服务器
        try {
            ChannelFuture f = b.group(group)//启动需要线程池，将线程池放入，指定用哪个group执行
                    .channel(NioSocketChannel.class)//指定连接到服务器的Channel类型 可以传BIO的 SocketChannel 实现阻塞版
                    //connect后 被初始化了后调ChannelInitializer
                    //handler 返回值是bootstrap connect是ChannelFuture 无法交换顺序
                    .handler(new ClientChannelInitializer())//当有事件来时谁去处理
                    //异步方法 Netty所有方法均为异步 方法调完后会继续运行 无论是否连接上
                    //sync方法 等待结束 确认执行完再往下走
                    //返回的ChannelFuture 当一件事情执行完会产生一个后果，结束时会被调用 observer
                    .connect("localhost", 8888);//连接远程服务器IP，端口号
            //将指定的监听器添加到这个future中。当此future完成时，将通知指定的侦听器。
            //如果这个future已经完成，则立即通知指定的侦听器。
            f.addListener(new ChannelFutureListener() {//添加监听器 future是否成功
                //结果出来调
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connect!");
                    } else {
                        System.out.println("connected!");
                    }
                }
            });

            f.sync();//异步 需要阻塞住，否则直接结束没有结果

            System.out.println("...");

            //[id: 0xb3d30a1c]
            //...
            //connected!
            //已经连接上 sync结束 future通知Listener 打印

        } finally {
            group.shutdownGracefully();//结束
        }
    }
}

//handler需要ChannelHandler ChannelInitializer 实现 ChannelHandler
//可以指定Channel类型 SocketChannel 网络连接
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {//Channel做初始化

    //初始化时 还没有连接 会被调用
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        System.out.println(ch);
    }
}
