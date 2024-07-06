package armadillo.decoder;

import armadillo.Constant;
import armadillo.utils.RSASignature;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class SmartByteBufDecoder extends ByteToMessageDecoder {
    private final Logger logger = Logger.getLogger(SmartByteBufDecoder.class);
    private final int base_length = 21;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (!ctx.channel().isOpen())
            return;
        if (buffer.readableBytes() < base_length)
            return;
        int beginIndex = buffer.readerIndex();
        byte[] magic = new byte[9];
        buffer.readBytes(magic);
        if (!Arrays.equals(magic, "Armadillo".getBytes())) {
            if (Constant.isDevelopment())
                logger.info(String.format("IP:%s -> Magic错误:%s", ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress(), new String(magic)));
            throw new DecoderException();
        }
        buffer.skipBytes(4);
        int dataLen = buffer.readInt();
        int signLen = buffer.readInt();
        int dataLength = dataLen + signLen;
        if (buffer.readableBytes() < dataLength) {
            buffer.readerIndex(beginIndex);
            return;
        } else if (buffer.readableBytes() > dataLength) {
            if (Constant.isDevelopment())
                logger.info(String.format("IP:%s -> 非法数据长度错误:%d", ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress(), buffer.readableBytes()));
            throw new DecoderException();
        }
        /*byte[] request = new byte[dataLen];
        byte[] sign = new byte[signLen];
        buffer.readBytes(request).readBytes(sign);
        if (!RSASignature.getInstance().doCheck(request, sign)) {
            if (Constant.isDevelopment())
                logger.info(String.format("IP:%s -> 数据验签失败", ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress()));
            throw new DecoderException();
        }*/
        buffer.resetReaderIndex();
        buffer.skipBytes(9);
        out.add(buffer.retainedDuplicate());
        buffer.skipBytes(buffer.readableBytes());
    }

}
