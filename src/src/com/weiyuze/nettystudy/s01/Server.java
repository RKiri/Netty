package com.weiyuze.nettystudy.s01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;


public class Server {
    //clients 一组Channel 默认的通道组 需要传executor 通道组处理的线程 默认的线程处理通道组上的事件
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void main(String[] args) throws IOException, InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//负责客户端连接 accept
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);//连接上后 socket上产生的事件交给work处理 数字代表线程数

        try {
            ServerBootstrap b = new ServerBootstrap();
            ChannelFuture f = b.group(bossGroup, workerGroup)//指定线程池两个组，一个用来接收客户端连接，一个处理客户端连接上的IO事件
                    .channel(NioServerSocketChannel.class)//指定Channel类型 均为异步 除非指定同步Channel
                    //.handler()//Server大面板以及已经client连接上的SocketChannel上
                    .childHandler(new ChannelInitializer<SocketChannel>() {//连接上后会调用
                        //初始化完成 已经连接上后调initChannel()  回调方法
                        //每一个客户端连接都会调用initChannel()
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //管道 流数据 可在上添加handler(责任处理器) 数据流经管道被handler处理 责任链 过滤器
                            //Inbound Handler、Outbound Handler 可写在一起 但一般情况下分开
                            ChannelPipeline pl = ch.pipeline();
                            //可添加自己的数据处理器 加在最后
                            //每一个客户端Pipeline上都加ServerChildHandler
                            pl.addLast(new ServerChildHandler());//Server和客户端连接的Channel的处理器

                            //[id: 0xbb8eb5eb, L:/127.0.0.1:8888 - R:/127.0.0.1:10379]
                            //[id: 0x9cb3977e, L:/127.0.0.1:8888 - R:/127.0.0.1:10387]
                            //L：服务器 R：客户端 随机指定
                            //System.out.println(ch);//打印IP和端口
                        }
                    })//Socket已经连接 只加在client端上
                    .bind(8888)//监听8888端口
                    //会产生一个ChannelFuture 可加监听器看bind是否成功 直到成功继续往下执行 一般不会不成功
                    .sync();//不写相当于bind完直接往下执行 不知道是否成功 万一出错
            //方法异步 添加sycn() 让其同步

            System.out.println("server started!");

            //channel() 拿到了绑定在ChannelFuture上的Channel Server的Channel
            //closeFuture() 如果有人调了close() 返回值是ChannelFuture;sync() 如果没有人调close() 会永远等待结果
            //调close() 执行这句话 继续往下执行
            f.channel().closeFuture().sync();//不会结束 阻塞
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

//ChannelInboundHandlerAdapter 处理读数据方法做骨架实现
class ServerChildHandler extends ChannelInboundHandlerAdapter { //SimpleChannleInboundHandler Codec 泛型

    //通道可以用时 可将通道放到通道组里
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clients.add(ctx.channel());
    }

    //接收数据 当管道上有数据写过来 调channelRead方法 将数据保存在msg(ByteBuf)
    //Context ChannelHandler的上下文
    //从workerGroup里拿一个线程调channelRead
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf) msg;

            byte[] bytes = new byte[buf.readableBytes()];//readableBytes有多少个(可以读的)字节
            buf.getBytes(buf.readerIndex(),bytes);//从可以读的指针位置开始 把数据读满 读到整个字节数组里
            System.out.println(new String(bytes));
            Server.clients.writeAndFlush(msg);//将通道组里每一个通道拿来往外写数据

            //ctx.writeAndFlush(msg);//自动释放 无需在finally释放

            //PooledUnsafeDirectByteBuf(ridx: 0, widx: 5, cap: 1024)
            //两个指针 读指针从0开始读 写指针从5开始写 已经装了“hello”默认1024字节大小
            //System.out.println(buf);

            //1 没有释放
            //System.out.println(buf.refCnt());//ReferenceCounted有几个引用 有多少指向了他
        } finally {
            //if (buf != null)ReferenceCountUtil.release(buf);//释放 内存没有泄露；只是读没有往外写 需要释放
            //System.out.println(buf.refCnt());
        }
    }

    //当有异常的时候 打印出来;Context close
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();//通知client端
    }
}
