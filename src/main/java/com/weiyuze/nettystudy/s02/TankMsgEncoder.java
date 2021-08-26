package com.weiyuze.nettystudy.s02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TankMsgEncoder extends MessageToByteEncoder<TankMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TankMsg msg, ByteBuf buf) throws Exception {
        //将TankMsg写到Bytebuf
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
    }
}
