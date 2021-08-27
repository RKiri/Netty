import com.weiyuze.nettystudy.s02.TankMsg;
import com.weiyuze.nettystudy.s02.TankMsgDecoder;
import com.weiyuze.nettystudy.s02.TankMsgEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

public class TankMsgCodecTest {

    @Test
    public void testTankMsgEncoder() {
        TankMsg msg = new TankMsg(10, 10);
        EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder());
        ch.writeOutbound(msg);

        ByteBuf buf = ch.readOutbound();
        int x = buf.readInt();
        int y = buf.readInt();

        //这个等式通过 测试通过
        Assert.assertTrue(x == 10 && y == 10);
        buf.release();

    }


    // Decoder解码 将buf转为msg 而msg不需要编码
    @Test
    public void testTankMsgEncoder2() {
        ByteBuf buf = Unpooled.buffer();
        TankMsg msg = new TankMsg(10, 10);
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);

        EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder(), new TankMsgDecoder());
        ch.writeInbound(buf.duplicate());

        TankMsg tm = ch.readInbound();

        Assert.assertTrue(tm.x == 10 && tm.y == 10);


    }
}
