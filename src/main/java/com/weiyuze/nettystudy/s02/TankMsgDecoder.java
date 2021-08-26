package com.weiyuze.nettystudy.s02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class TankMsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //没有读全 最少8个字节
        //TCP 拆包 粘包的问题
        if (in.readableBytes() < 8) return;

        //先写先读
        int x = in.readInt();
        int y = in.readInt();

        //消息解析出来的对象 装进List
        out.add(new TankMsg(x,y));
    }
}
