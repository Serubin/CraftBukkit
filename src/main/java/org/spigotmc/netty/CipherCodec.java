package org.spigotmc.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;
import org.bouncycastle.crypto.BufferedBlockCipher;

/**
 * This class is a complete solution for encrypting and decoding bytes in a
 * Netty stream. It takes two {@link BufferedBlockCipher} instances, used for
 * encryption and decryption respectively.
 */
public class CipherCodec extends ByteToByteCodec {

    private BufferedBlockCipher encrypt;
    private BufferedBlockCipher decrypt;
    private ByteBuf heapOut;

    public CipherCodec(BufferedBlockCipher encrypt, BufferedBlockCipher decrypt) {
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (heapOut == null) {
            heapOut = ctx.alloc().heapBuffer();
        }
        cipher(encrypt, in, heapOut);
        out.writeBytes(heapOut);
        heapOut.discardSomeReadBytes();
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        cipher(decrypt, in, out);
    }

    @Override
    public void freeInboundBuffer(ChannelHandlerContext ctx) throws Exception {
        super.freeInboundBuffer(ctx);
        decrypt = null;
    }

    @Override
    public void freeOutboundBuffer(ChannelHandlerContext ctx) throws Exception {
        super.freeOutboundBuffer(ctx);
        if (heapOut != null) {
            heapOut.release();
            heapOut = null;
        }
        decrypt = null;
    }

    private void cipher(BufferedBlockCipher cipher, ByteBuf in, ByteBuf out) {
        int available = in.readableBytes();
        int outputSize = cipher.b(available); // getUpdateOutputSize
        if (out.capacity() < outputSize) {
            out.capacity(outputSize);
        }
        int processed = cipher.a(in.array(), in.arrayOffset() + in.readerIndex(), available, out.array(), out.arrayOffset() + out.writerIndex()); // processBytes
        in.readerIndex(in.readerIndex() + processed);
        out.writerIndex(out.writerIndex() + processed);
    }
}
