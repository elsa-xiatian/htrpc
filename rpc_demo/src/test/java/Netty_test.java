import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import netty.client;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Netty_test {
    @Test
    public void testCompositByteBuf(){
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        //通过逻辑组装而非物理拷贝
        byteBuf.addComponents(header,body);
    }

    @Test
    public void testWrapper(){
       byte[] buf = new byte[1024];
       byte[] buf2 = new byte[1024];
       //共享byte数组的内容
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }

    public void tsetMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("xht".getBytes(StandardCharsets.UTF_8));//魔术值
        message.writeByte(1);//版本号
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(3);
        message.writeLong(251455L);
        client aclinet = new client();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(aclinet);

    }

    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12,12,13,13,12,12,12,24,25,23,12,12};

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);

        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = bos.toByteArray();
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void DetestCompress() throws IOException {
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, -31, -31, -27, -27, -31, -31, -111, -112, 20, -25, -31, 1, 0, 10, -72, 82, 86, 12, 0, 0, 0};

        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bis);



        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(Arrays.toString(bytes));
    }
}
