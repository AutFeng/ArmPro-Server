package armadillo.helper;

import armadillo.Application;
import armadillo.Constant;
import armadillo.controller.SocketController;
import armadillo.utils.LoaderRes;
import armadillo.utils.RSAUtils;
import armadillo.utils.StreamUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = Logger.getLogger(ServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            try {
                if (HttpMethod.GET == httpRequest.method()) {
                    URI uri = new URI(httpRequest.uri());
                    if ("/get".equalsIgnoreCase(uri.getPath())) {
                        Map<String, String> parmMap = new HashMap<>();
                        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
                        decoder.parameters().forEach((key, value) -> parmMap.put(key, value.get(0)));
                        String key = parmMap.get("key");
                        if (key == null)
                            ctx.close();
                        else {
                            FullHttpResponse response = new DefaultFullHttpResponse(
                                    HTTP_1_1, OK, Unpooled.wrappedBuffer(StreamUtil.readBytes(new FileInputStream(new File(Constant.getIcon(), key + ".png")))));
                            response.headers().set(CONTENT_TYPE, "image/png");
                            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                        }
                    } else if ("/file".equalsIgnoreCase(uri.getPath())) {
                        Map<String, String> parmMap = new HashMap<>();
                        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
                        decoder.parameters().forEach((key, value) -> parmMap.put(key, value.get(0)));
                        String key = parmMap.get("key");
                        if (key == null)
                            ctx.close();
                        else {
                            FullHttpResponse response = new DefaultFullHttpResponse(
                                    HTTP_1_1, OK, Unpooled.wrappedBuffer(StreamUtil.readBytes(new FileInputStream(new File(Constant.getTask(), key)))));
                            response.headers().set(CONTENT_TYPE, "application/octet-stream");
                            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;
                        }
                    } else if ("/ver".equalsIgnoreCase(uri.getPath())) {
                        Map<String, String> parmMap = new HashMap<>();
                        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
                        decoder.parameters().forEach((key, value) -> parmMap.put(key, value.get(0)));
                        String key = parmMap.get("key");
                        if (key == null)
                            ctx.close();
                        else {
                            FullHttpResponse response = new DefaultFullHttpResponse(
                                    HTTP_1_1, OK, Unpooled.wrappedBuffer(StreamUtil.readBytes(new FileInputStream(new File(Constant.getRoot(), key + ".apk")))));
                            response.headers().set(CONTENT_TYPE, "application/octet-stream");
                            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;
                        }
                    } else
                        ctx.close();
                } else
                    ctx.close();
            } finally {
                httpRequest.release();
            }
        } else if (msg instanceof ByteBuf)
            ctx.channel().eventLoop().execute(new SocketController(ctx, (ByteBuf) msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.toString());
        ctx.close();
    }
}
